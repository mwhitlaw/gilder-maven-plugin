/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.ancillary;


import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;
import com.allegianttravel.ota.framework.module.spi.ManagedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.concurrent.Future;

import static com.allegianttravel.ota.flight.shop.g4.ancillary.G4AncillaryUtil.getFlightFeesForDaysOfTravel;

/**
 * Asynchronously gets the flight fees and stashes them on the exchange
 */
@Named
public class G4AncillaryAsyncFilter implements Filter {

    static final String FUTURE_KEY = "g4-ancillary";
    private static final Logger logger = LoggerFactory.getLogger(G4AncillaryAsyncFilter.class);

    /**
     * Used to schedule the FareMaker call
     */
    private ManagedExecutorService executorService;

    @Inject
    public G4AncillaryAsyncFilter(ManagedExecutorService managedExecutorService) {
        this.executorService = managedExecutorService;
    }

    /**
     * No-arg ctor required by CDI
     */
    @SuppressWarnings("unused")
    public G4AncillaryAsyncFilter() {}

    @Override
    @Profile
    public void process(Exchange exchange) {

        if (Boolean.parseBoolean(G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENABLED.getValue())) {

            logger.debug("ancillary fees are enabled");

            FlightShopRequest shopRequest = exchange.getIn(FlightShopRequest.class);

            Future<FlightFees> results =
                    executorService.submit(() -> getFlightFeesForDaysOfTravel(shopRequest));

            exchange.setProperty(FUTURE_KEY, results);
        } else {
            logger.debug("ancillary fees are disabled");
            exchange.setProperty(FUTURE_KEY, Collections.emptyMap());
        }
    }
}
