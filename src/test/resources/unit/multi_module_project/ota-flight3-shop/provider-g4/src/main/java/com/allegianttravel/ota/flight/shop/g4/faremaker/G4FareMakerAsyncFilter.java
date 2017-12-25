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

import com.allegianttravel.ota.faremaker.FareMakerResults;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;
import com.allegianttravel.ota.framework.module.spi.ManagedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.Future;

import static com.allegianttravel.ota.flight.shop.g4.faremaker.G4FareMakerUtil.invokeFareMaker;


/**
 * Filter that runs before the search in order to start the FareMaker search running so it can run in parallel with
 * the G4 search.
 *
 * The results of this Filter will be a Future object in the exchange that the G4 providers will access in order to
 * get the results.
 */
@Named
public class G4FareMakerAsyncFilter implements Filter {

    static final String FUTURE_KEY = "g4-fare-maker";

    private static final Logger logger = LoggerFactory.getLogger(G4FareMakerAsyncFilter.class);

    /**
     * Used to schedule the FareMaker call
     */
    private ManagedExecutorService executorService;

    @Inject
    public G4FareMakerAsyncFilter(ManagedExecutorService managedExecutorService) {
        this.executorService = managedExecutorService;
    }

    /**
     * No-arg ctor required by CDI
     */
    public G4FareMakerAsyncFilter() {}


    @Override
    public void process(Exchange exchange) {
        String value = G4FlightProperties.G4_FARE_MAKER_ENABLED.getValue();
        if (Boolean.parseBoolean(value)) {
            Future<FareMakerResults> future = executorService.submit(() -> {
                FlightShopRequest shopRequest = exchange.getIn(FlightShopRequest.class);
                return invokeFareMaker(shopRequest);
            });
            exchange.setProperty(FUTURE_KEY, future);
        } else {
            logger.debug("fare-maker is disabled, not searching for new fares");
        }
    }

}
