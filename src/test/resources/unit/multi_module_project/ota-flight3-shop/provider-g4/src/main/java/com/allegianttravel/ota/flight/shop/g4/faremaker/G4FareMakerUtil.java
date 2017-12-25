/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.faremaker;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiantair.g4flights.domain.Channel;
import com.allegianttravel.ota.faremaker.FareMakerResults;
import com.allegianttravel.ota.faremaker.NewFare;
import com.allegianttravel.ota.faremaker.rest.FareMakerResourceClient;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequestProviderInput;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.FareRuleType;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class G4FareMakerUtil {
    public static final String G4 = "g4";
    private static final Logger logger = LoggerFactory.getLogger(G4FareMakerUtil.class);

    private G4FareMakerUtil() {
    }

    public static FareMakerResults invokeFareMaker(FlightShopRequest shopRequest) throws FareMakerException {
        String endpoint = G4FlightProperties.G4_FARE_MAKER.makeEndpoint("/");

        FareMakerResourceClient fareMakerResourceClient = ServiceGenerator.create(FareMakerResourceClient.class, endpoint);

        Call<FareMakerResults> call = fareMakerResourceClient.getFareOffers(
                shopRequest.getDepartAirportCode().toString(),
                shopRequest.getArriveAirportCode().toString(),
                shopRequest.getDepartDate().toString(),
                shopRequest.getReturnDate() != null ? shopRequest.getReturnDate().toString() : null,
                shopRequest.getReqPlusDays(), shopRequest.getReqMinusDays(),
                shopRequest.getBookingDate().toString(),
                Channel.getChannelById(shopRequest.getChannelId()).name(),
                shopRequest.getPassengerCount(),
                shopRequest.getFareRuleType() == FareRuleType.ROUNDTRIP
                );
        try {
            Response<FareMakerResults> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw new FareMakerException(Severity.WARNING, "Error response from faremaker",
                        call.request().url().toString(),
                        response.code(), response.errorBody().string(), null);
            }
        } catch (IOException e) {
            logger.warn("Error getting fares {}", call.request().url(), e);
            throw new FareMakerException(Severity.WARNING, "Error invoking faremaker",
                    call.request().url().toString(), e);
        }
    }

    public static void decorateG4Flights(FareMakerResults results, ProviderFlightShopResponse response,
                                  FlightShopRequestProviderInput input) {

        boolean filterPresent = input.getNamedFare().isPresent();

        //noinspection ConstantConditions
        Predicate<NewFare> filter = newFare -> !filterPresent || input.getNamedFare().get().equals(newFare.getName());
        // todo - need to add some logging to indicate what got removed and why. This would go a long way in debugging

        FlightShopResponse flightShopResponse = response.getResponse();
        if (!flightShopResponse.isEmpty()) {
            flightShopResponse.getFlightOptions().values()
                    .stream()
                    .flatMap(flightOptions -> flightOptions.getFlightOptions().stream())
                    .filter(flightOption -> G4.equals(flightOption.getProviderFares().get(0).getProviderId()))
                    .forEach(flightOption -> G4PackageDecorator.decorate(flightOption, results, filter));
        }

        if (filterPresent && !ProviderFares.STD_RATE.equals(input.getNamedFare().get())) {
            // we need to remove the STD fare from the results....
            removeStandardFare(response);
        }

    }

    private static void removeStandardFare(ProviderFlightShopResponse response) {
        response.getResponse().getFlightOptions().values()
                .stream()
                .flatMap(flightOptions -> flightOptions.getFlightOptions().stream())
                .flatMap(flightOption -> flightOption.getProviderFares().stream())
                .forEach(providerFares -> providerFares.setNamedFares(providerFares.getNamedFares()
                        .stream()
                        .filter(namedFare -> !ProviderFares.STD_RATE.equals(namedFare.getName()))
                        .collect(Collectors.toList())));
    }
}
