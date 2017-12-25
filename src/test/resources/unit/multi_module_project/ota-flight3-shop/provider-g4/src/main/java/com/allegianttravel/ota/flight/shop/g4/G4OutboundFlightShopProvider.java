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

@Named
public class G4OutboundFlightShopProvider extends AbstractG4FlightShopProvider {

    private static final Logger logger = LoggerFactory.getLogger(G4OutboundFlightShopProvider.class);

    @Override
    @Profile
    public ProviderOutput provide(ProviderInput input) {
        return super.provide(input);
    }

    @Override
    FlightShopResponse query(FlightShopRequest shopRequest) {
        logger.debug("querying outbound flights in g4 for {}", shopRequest);

        return super.query(shopRequest);
    }

    @Override
    public String getName() {
        return "g4-outbound";
    }

    @Override
    public String getDescription() {
        return "G4 Flight3 Shop Outbound";
    }

}
