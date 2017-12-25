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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FlightShopUtils {

    private FlightShopUtils() {}

    static Map<FlightRequest, FlightOptions> groupByFlightRequest(FlightShopRequest shopRequest,
                                                                  FlightOptions flightOptions) {
        // put the oneway or round trip flights into their city pair buckets
        // for oneway, they'll all be in one bucket
        // for round trip, we'll have an outbound and inbound bucket
        // this is done to keep the responses for the different flight searches consistent
        Map<FlightRequest, FlightOptions> optionsByRequest = new LinkedHashMap<>();
        FlightRequest outboundRequest = new FlightRequest(shopRequest.getDepartAirportCode(),
                shopRequest.getArriveAirportCode(), shopRequest.getDepartDate());
        FlightRequest inboundRequest = shopRequest.getReturnDate() == null ? null :
                new FlightRequest(shopRequest.getArriveAirportCode(),
                        shopRequest.getDepartAirportCode(),
                        shopRequest.getReturnDate());
        for (FlightOption fo : flightOptions.getFlightOptions()) {
            FlightOptions options;
            if (fo.getFirstOrigin().equals(outboundRequest.getOrigin())) {
                options = optionsByRequest.computeIfAbsent(outboundRequest, o -> new FlightOptions());
            } else {
                options = optionsByRequest.computeIfAbsent(inboundRequest, o -> new FlightOptions());
            }
            options.addFlightOption(fo);
        }
        return optionsByRequest;
    }

    public static ProviderFlightShopResponse toProviderResponse(FlightShopRequest shopRequest, FlightOptions result) {
        Map<FlightRequest, FlightOptions> optionsByRequest = groupByFlightRequest(shopRequest, result);

        FlightShopResponse response = new FlightShopResponse();
        response.setFlightOptions(optionsByRequest);
        return new ProviderFlightShopResponse(response);
    }

}
