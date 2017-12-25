/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.provider;

import com.allegiant.tests.LoadFromClasspath;
import com.allegiant.util.gen2.JsonUtils;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightKey;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class FilterFixture {

    public static final AirportCode BOS = new AirportCode("BOS");
    public static final AirportCode LAS = new AirportCode("LAS");
    public static final AirportCode BLI = new AirportCode("BLI");
    public static final AirportCode LAX = new AirportCode("LAX");

    /**
     * Creates just the pieces we need within a FlightOption in order to exercise our tests
     * @param key
     * @param baseFare
     * @param providerId
     * @param packageId
     */
    static FlightOption createFlightOption(AirportCode origin, AirportCode destination, FlightKey key, Money baseFare, String providerId, PackageId packageId) {
        FlightOption option = new FlightOption();
        Segment seg = new Segment();
        seg.setCarrierCode(key.getAirline());
        seg.setFlightNumber(key.getFlightNumber());
        seg.setLegs(Collections.singletonList(
                new Leg()
                        .setOrigin(origin)
                        .setDestination(destination)
                        .setDepartureTime(key.getDeparture().atTime(1, 10))

        ));
        option.setSegments(Collections.singletonList(seg));
        ProviderFares providerPricingOption = new ProviderFares()
                .setProviderId(providerId)
                .setPackageId(packageId);
        if (baseFare != null) {
            providerPricingOption.addNamedFare(new NamedFare().setFareDetails(Collections.singletonList(new FareDetails().setBaseFare(baseFare))));
        }
        option.setProviderFares(Collections.singletonList(providerPricingOption));
        return option;
    }

    static Map<FlightRequest, FlightOptions> groupByFlightRequest(FlightShopRequest request, String payloadPath) throws Exception {
        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        FlightOptions options = om.readValue(LoadFromClasspath.asString(payloadPath), FlightOptions.class);

        Map<FlightRequest, FlightOptions> optionsByRequest = FlightShopUtils.groupByFlightRequest(request, options);
        return optionsByRequest;
    }

}
