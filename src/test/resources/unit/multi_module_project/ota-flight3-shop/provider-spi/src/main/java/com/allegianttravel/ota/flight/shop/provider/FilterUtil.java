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

import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;

import java.util.List;
import java.util.Optional;

public final class FilterUtil {

    private FilterUtil() {}

    public static Optional<FlightShopResponse> getResponse(Exchange exchange) {
        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);

        if (outputs.isEmpty()) {
            return Optional.empty();
        } else {
            ProviderFlightShopResponse response = outputs.get(0);
            FlightShopResponse flightShopResponse = response.getResponse();
            return Optional.of(flightShopResponse);
        }
    }

    public static void clearOutput(Exchange exchange) {
        while (!exchange.getOuts().isEmpty()) {
            exchange.removeOut(0);
        }
    }
}
