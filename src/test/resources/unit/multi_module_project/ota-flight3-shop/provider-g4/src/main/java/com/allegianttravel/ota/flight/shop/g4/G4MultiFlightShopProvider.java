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

import com.allegianttravel.ota.flight.shop.g4.markets.MarketService;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.provider.ProviderMultiCityShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.MultiCityShopRequest;
import com.allegianttravel.ota.flight3.MarketName;
import com.allegianttravel.ota.framework.module.spi.ManagedExecutorService;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.allegianttravel.ota.flight.shop.g4.G4ShopClientHelper.fork;
import static com.allegianttravel.ota.flight.shop.g4.G4ShopClientHelper.join;

/**
 * MultiCity shopping provider for G4
 *
 * Accepts multiple city pairs and returns flight options for each of the pairs that are valid markets.
 */
@Named
public class G4MultiFlightShopProvider implements Provider {
    private static final Logger logger = LoggerFactory.getLogger(G4MultiFlightShopProvider.class);

    /**
     * Used to execute the REST calls in parallel
     */
    private ManagedExecutorService executorService;

    /**
     * Used to determine if the market is available before sending a search request to G4. We can cache the results of
     * this check so it's better to check before calling since G4 will respond with an error if the
     */
    private MarketService marketService;


    @Inject
    public G4MultiFlightShopProvider(ManagedExecutorService managedExecutorService, MarketService injectedMarketService) {
        this.executorService = managedExecutorService;
        this.marketService = new CachedMarketServiceImpl(injectedMarketService);
    }

    /**
     * Required by CDI
     */
    public G4MultiFlightShopProvider() {}

    /**
     * Executes the flight search which may include markets that
     * aren't service by a one-way.
     * @param input
     * @return
     */
    @Override
    @Profile
    public ProviderOutput provide(ProviderInput input) {

        ProviderMultiCityShopRequest providerMultiCityShopRequest = (ProviderMultiCityShopRequest) input;
        MultiCityShopRequest multiCityShopRequest = providerMultiCityShopRequest.getMultiCityShopRequest();

        logger.debug("servicing multi city request {}", multiCityShopRequest);

        List<G4Callable> g4ShopRequests = toShopRequests(multiCityShopRequest);

        logger.debug("prepared {} g4 shop calls", g4ShopRequests.size());

        try {
            List<G4ShopResponse> responses = join(fork(executorService, g4ShopRequests));

            G4ShopDtoToFlightOptionMapper mapper = new G4ShopDtoToFlightOptionMapper(responses,
                    multiCityShopRequest.getPassengers());

            Map<FlightRequest, FlightOptions> optionsByRequest = mapper.groupByPairAndTravelDate();

            FlightShopResponse response = new FlightShopResponse();
            response.setFlightOptions(optionsByRequest);

            return new ProviderFlightShopResponse(response);
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Error making async shop call", e);
            FlightShopResponse response = new FlightShopResponse();
            response.setErrorMessages(Collections.singletonList(
                    new ErrorMessage()
                            .setSeverity(Severity.ERROR)
                            .setProviderId("g4")
                            .setMessage(e.getMessage())));
            return new ProviderFlightShopResponse(response);
        }
    }

    /**
     * Converts the shop request into G4 shop callables. Each {@link FlightRequest} becomes a call into G4 for its
     * flights provided that the market is one served by G4.
     * @param multiCityShopRequest
     * @return
     */
    private List<G4Callable> toShopRequests(MultiCityShopRequest multiCityShopRequest) {

        return multiCityShopRequest.getFlights()
                .stream()
                .map(fr -> FlightShopRequest
                        .builder()
                        .setDepartAirportCode(fr.getOrigin())
                        .setArriveAirportCode(fr.getDestination())
                        .setDepartDate(fr.getTravelDate())
                        .setReqMinusDays(fr.getReqMinusDays())
                        .setReqPlusDays(fr.getReqPlusDays())
                        .setReturnDate(null)
                        .setChannelId(multiCityShopRequest.getChannelId())
                        .setPassengers(multiCityShopRequest.getPassengers())
                        .setBookingDate(multiCityShopRequest.getBookingDate())
                        .setBookingType(multiCityShopRequest.getBookingType())
                        .setCouponCodes(multiCityShopRequest.getCouponCodes())
                        .build()
                )
                // Filter out any markets that aren't served by G4
                .filter(fr -> marketService.isAllowed(
                        new MarketName(
                                fr.getDepartAirportCode().toString(),
                                fr.getArriveAirportCode().toString())))
                .map(G4ShopClientHelper::toShopRequests)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "g4-multi";
    }

    @Override
    public String getDescription() {
        return "G4 Flight3 Multi Shop";
    }


    @Override
    public int clearCaches() {
        return 0;
    }

}
