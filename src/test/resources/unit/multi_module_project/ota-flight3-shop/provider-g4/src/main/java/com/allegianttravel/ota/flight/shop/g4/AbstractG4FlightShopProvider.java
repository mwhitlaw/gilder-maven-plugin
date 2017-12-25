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
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.allegianttravel.ota.flight.shop.g4.G4ShopClientHelper.invoke;

/**
 * ota-framework Provider for the G4 flight shop call
 */
public abstract class AbstractG4FlightShopProvider implements Provider {

    private static final Logger logger = LoggerFactory.getLogger(AbstractG4FlightShopProvider.class);

    @Override
    public ProviderOutput provide(ProviderInput input) {
        FlightShopRequest shopRequest = (FlightShopRequest) input;

        try {
            FlightShopResponse response = query(shopRequest);
            return new ProviderFlightShopResponse(response);
        } catch (RuntimeException e) {
            logger.error("Error invoking G4 service", e);
            throw e;
        }
    }

    @Override
    public int clearCaches() {
        return 0;
    }

    /**
     * Queries the underlying G4 provider for flight options with the given request
     * @param shopRequest
     * @return
     */
    FlightShopResponse query(FlightShopRequest shopRequest) {

        G4ShopResponse response = invoke(shopRequest);

        logger.debug("got {} responses", response.size());

        Map<FlightRequest, FlightOptions> optionsByRequest = G4ShopClientHelper.toOptionsByRequest(response, shopRequest);

        if (logger.isTraceEnabled()) {
            optionsByRequest.forEach((key, value) -> {
                logger.trace("G4 entries for {}", key);
                value.getFlightOptions().forEach(fo -> logger.trace("G4 entry: {}", fo.getSegmentKeys()));
            });
        }

        FlightShopResponse flightShopResponse = new FlightShopResponse();
        flightShopResponse.setFlightOptions(optionsByRequest);
        flightShopResponse.setErrorMessages(response.getErrorMessages());
        return flightShopResponse;
    }
}
