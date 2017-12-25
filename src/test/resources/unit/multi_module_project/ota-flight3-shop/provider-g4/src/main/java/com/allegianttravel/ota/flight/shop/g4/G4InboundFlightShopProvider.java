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

import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collections;

@Named
public class G4InboundFlightShopProvider extends AbstractG4FlightShopProvider {
    private static final Logger logger = LoggerFactory.getLogger(G4InboundFlightShopProvider.class);

    @Profile
    @Override
    public ProviderOutput provide(ProviderInput input) {
        return super.provide(input);
    }

    @Override
    FlightShopResponse query(FlightShopRequest shopRequest) {

        if (shopRequest.getReturnDate() != null) {
            logger.debug("querying inbound flights in g4 for {}", shopRequest);
            FlightShopRequest request = FlightShopRequest.builder(shopRequest)
                    .setDepartDate(shopRequest.getReturnDate())
                    .setDepartAirportCode(shopRequest.getArriveAirportCode())
                    .setArriveAirportCode(shopRequest.getDepartAirportCode())
                    .setReturnDate(null)
                    .build();
            return super.query(request);
        } else {
            logger.debug("it's a oneway search, no inbound flights");
            return new FlightShopResponse().setFlightOptions(Collections.emptyMap());
        }
    }

    @Override
    public String getName() {
        return "g4-inbound";
    }

    @Override
    public String getDescription() {
        return "G4 Flight3 Shop Inbound";
    }
}
