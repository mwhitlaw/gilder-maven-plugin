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

import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.allegianttravel.ota.flight.shop.provider.CollectorsUtil.throwingMerger;

/**
 * Aggregates results from multiple providers into a single response.
 */
@Named
public class FlightShopResponseAggregatorFilter implements Filter {

    @Override
    @Profile
    public void process(Exchange exchange) {
        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);

        // The unified response that we'll return
        FlightShopResponse unified = new FlightShopResponse();

        // tmp map of all of the output from different providers by FlightRequest
        Map<FlightRequest, List<FlightOptions>> map = new LinkedHashMap<>();

        // walk the outputs and put its data into the map
        for (ProviderFlightShopResponse providerResponse : outputs) {
            FlightShopResponse response = providerResponse.getResponse();

            if (response.getErrorMessages() != null) {
                List<ErrorMessage> errorMessages = unified.getErrorMessages();
                if (errorMessages == null) {
                    errorMessages = new ArrayList<>();
                    unified.setErrorMessages(errorMessages);
                }
                errorMessages.addAll(response.getErrorMessages());
            }

            if (!response.isEmpty()) {
                response.getFlightOptions().forEach((key, value) -> {
                    // add this provider's output to the map
                    List<FlightOptions> flightOptions = map.computeIfAbsent(key, k -> new ArrayList<>());
                    flightOptions.add(value);
                });
            }
        }

        // merge the different FlightOptions for a given FlightRequest so we don't have multiple objects for the same
        // exact flight. This would be the case if multiple providers served the same flight data. For example, QPX
        // and Sabre could return the same flight. We want to merge these so there's only a single flight that has
        // multiple pricing options, one for each provider that served it.
        Map<FlightRequest, FlightOptions> merged = map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> MergeFlightOptions.merge(e.getValue().stream()),
                        throwingMerger(),
                        LinkedHashMap::new));

        // we remove duplicate entries where two providers are serving the exact same flight option. There's no reason
        // to show the exact same flight that can be serviced from different providers.
        MergeFlightOptions.removeDupes(merged);

        unified.setFlightOptions(merged);

        while (!exchange.getOuts().isEmpty()) {
            exchange.removeOut(0);
        }

        exchange.addOut(new ProviderFlightShopResponse(unified));
    }
}
