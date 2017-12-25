/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiantair.g4flights.service.client.dto.StatusDto;
import com.allegiantair.g4flights.service.client.response.ShopFlightResponseDto;
import com.allegianttravel.ota.faremaker.FareMakerResults;
import com.allegianttravel.ota.flight.shop.g4.ancillary.AncillaryException;
import com.allegianttravel.ota.flight.shop.g4.ancillary.G4AncillaryUtil;
import com.allegianttravel.ota.flight.shop.g4.client.G4ShopClient;
import com.allegianttravel.ota.flight.shop.g4.faremaker.FareMakerException;
import com.allegianttravel.ota.flight.shop.g4.faremaker.G4FareMakerUtil;
import com.allegianttravel.ota.flight.shop.provider.CsvQueryParam;
import com.allegianttravel.ota.flight.shop.provider.FlightNumberRequest;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.provider.SupportingServiceException;
import com.allegianttravel.ota.flight.shop.rest.FareRuleType;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.spi.ManagedExecutorService;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.allegianttravel.ota.flight.shop.g4.G4ErrorMessageUtil.extractErrorMessages;
import static com.allegianttravel.ota.flight.shop.g4.G4ShopCallUtil.execute;
import static com.allegianttravel.ota.flight.shop.provider.SupportingServiceExceptionUtil.handleSupportingServiceException;

@Named
public class G4FlightByFlightNumProvider implements Provider {

    private static final Logger logger = LoggerFactory.getLogger(G4FlightByFlightNumProvider.class);

    /**
     * Used to schedule the FareMaker call
     */
    // Not used unless we need to add ancillary fees
    private ManagedExecutorService executorService;

    @Inject
    public G4FlightByFlightNumProvider(ManagedExecutorService managedExecutorService) {
        this.executorService = managedExecutorService;
    }

    // needed for CDI
    public G4FlightByFlightNumProvider() {}


    @Override
    public ProviderOutput provide(ProviderInput input) {

        FlightNumberRequest shopRequest = (FlightNumberRequest) input;

        ProviderFlightShopResponse providerResponse = shopByFlightNumber(shopRequest);

        if (!providerResponse.getResponse().getFlightOptions().isEmpty()) {
            // once we have the response, we need to decorate with fees and fare maker
            // we can't do this as part of the regular shop call since the market isn't provided

            FlightRequest flightRequest = providerResponse.getResponse().getFlightOptions().keySet().iterator().next();

            FlightShopRequest flightShopRequest = FlightShopRequest.builder()
                    .setCouponCodes(shopRequest.getCouponCodes())
                    .setArriveAirportCode(flightRequest.getDestination())
                    .setDepartAirportCode(flightRequest.getOrigin())
                    .setDepartDate(shopRequest.getDepartDate())
                    .setBookingDate(shopRequest.getBookingDate())
                    .setChannelId(shopRequest.getChannelId())
                    .setPassengers(shopRequest.getPassengers())
                    .setCallerMetadata(shopRequest.getCallerMetadata())
                    .build();

            Boolean fareMakerEnabled = Boolean.valueOf(G4FlightProperties.G4_FARE_MAKER_ENABLED.getValue());
            Boolean ancillaryEnabled = Boolean.valueOf(G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENABLED.getValue());

            logger.debug("ancillary fees enabled: {}, faremaker enabled: {}", ancillaryEnabled, fareMakerEnabled);

            final FlightFees flightFees;
            final FareMakerResults fareMakerResults;

            if (ancillaryEnabled && fareMakerEnabled) {
                logger.debug("fetching fees and faremaker results in parallel");
                Future<FareMakerResults> fareMakerResultsFuture = executorService.submit(() ->
                        G4FareMakerUtil.invokeFareMaker(flightShopRequest));
                Future<FlightFees> flightFeesFuture = executorService.submit(() ->
                        G4AncillaryUtil.getFlightFeesForDaysOfTravel(flightShopRequest));

                // If the async call to one of our supporting services fails then we want to propagate the error so it's
                // not a mystery as to why some data is missing.
                Consumer<ExecutionException> exceptionHandler = e -> {
                    if (e.getCause() instanceof SupportingServiceException) {
                        SupportingServiceException cause = (SupportingServiceException) e.getCause();
                        handleSupportingServiceException("g4", cause, providerResponse.getResponse());
                    }
                };

                fareMakerResults = getFromFutureOrNull("Error getting faremaker results",
                        fareMakerResultsFuture, exceptionHandler);
                flightFees = getFromFutureOrNull("Error getting ancillary fees",
                        flightFeesFuture, exceptionHandler);

            } else if (fareMakerEnabled) {
                FareMakerResults fmResults = null;
                try {
                    fmResults = G4FareMakerUtil.invokeFareMaker(flightShopRequest);
                } catch (FareMakerException e) {
                    handleSupportingServiceException("g4", e, providerResponse.getResponse());
                }
                fareMakerResults = fmResults;
                flightFees = null;
            } else if (ancillaryEnabled) {
                FlightFees ffResponse = null;
                try {
                    ffResponse = G4AncillaryUtil.getFlightFeesForDaysOfTravel(flightShopRequest);
                } catch (AncillaryException e) {
                    handleSupportingServiceException("g4", e, providerResponse.getResponse());
                }
                flightFees = ffResponse;
                fareMakerResults = null;
            } else {
                fareMakerResults = null;
                flightFees = null;
            }

            if (flightFees != null) {
                providerResponse.getResponse().setFlightFees(flightFees);
            }

            if (fareMakerResults != null) {
                G4FareMakerUtil.decorateG4Flights(fareMakerResults, providerResponse, shopRequest);
            }
        }

        return providerResponse;
    }


    private <T> T getFromFutureOrNull(String messageOnFailure, Future<T> future, Consumer<ExecutionException> exceptionHandler) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            exceptionHandler.accept(e);
            return null;
        } catch (InterruptedException e) {
            logger.warn(messageOnFailure, e);
            return null;
        }
    }

    private ProviderFlightShopResponse shopByFlightNumber(FlightNumberRequest shopRequest) {
        String endpoint = G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.makeEndpoint("/");
        final G4ShopClient g4Client = ServiceGenerator.create(G4ShopClient.class, endpoint);

        final String couponCodes = CsvQueryParam.toCSV(shopRequest.getCouponCodes());
        Call<ShopFlightResponseDto> call = g4Client.shopByFlightNumber(shopRequest.getFlightNumber(),
                shopRequest.getDepartDate(),
                shopRequest.getChannelId(),
                shopRequest.getFareRuleType() == FareRuleType.ROUNDTRIP,
                shopRequest.getPassengerCount(),
                shopRequest.getBookingDate(),
                shopRequest.getBookingType(),
                couponCodes,
                shopRequest.getFareClass());



        ShopFlightResponseDto shopFlightResponseDto;

        try {
            shopFlightResponseDto = execute(call, ShopFlightResponseDto.class, ShopFlightResponseDto::new);
        } catch (IOException e) {
            shopFlightResponseDto = new ShopFlightResponseDto();
            shopFlightResponseDto.setError(new StatusDto(e.getClass().getSimpleName(), e.getMessage()));
        }

        G4ShopResponse g4ShopResponse = new G4ShopResponse(shopFlightResponseDto, shopRequest.getDepartDate());
        Map<FlightRequest, FlightOptions> optionsByRequest = G4ShopClientHelper.toOptionsByRequest(g4ShopResponse, shopRequest);
        FlightShopResponse flightShopResponse = new FlightShopResponse();
        flightShopResponse.setFlightOptions(optionsByRequest);
        flightShopResponse.setErrorMessages(extractErrorMessages(shopFlightResponseDto));

        return new ProviderFlightShopResponse(flightShopResponse);
    }

    @Override
    public String getName() {
        return "g4-flightByNumber";
    }

    @Override
    public String getDescription() {
        return "G4 Flight3 Shop By Flight Number";
    }

    @Override
    public int clearCaches() {
        return 0;
    }
}
