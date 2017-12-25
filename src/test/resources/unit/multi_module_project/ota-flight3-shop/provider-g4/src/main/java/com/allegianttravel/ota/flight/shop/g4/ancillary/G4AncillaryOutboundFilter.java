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
import com.allegianttravel.ota.flight.shop.provider.FilterUtil;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.provider.SupportingServiceException;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.allegianttravel.ota.flight.shop.provider.SupportingServiceExceptionUtil.handleSupportingServiceException;

@Named
public class G4AncillaryOutboundFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(G4AncillaryOutboundFilter.class);

    @Override
    @Profile
    public void process(Exchange exchange) {

        // don't do anything if the ancillary fees are turned off
        if (!Boolean.parseBoolean(G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENABLED.getValue())) {
            return;
        }

        try {

            @SuppressWarnings("unchecked") Future<FlightFees> results =
                    (Future<FlightFees>)
                    exchange.getProperty(G4AncillaryAsyncFilter.FUTURE_KEY);
            // if the results are missing, return empty search results to match v2's behavior
            if (results == null) {
                throw new MissingAncillaryFeesException();
            }

            Optional<FlightShopResponse> optResponse = FilterUtil.getResponse(exchange);
            if (optResponse.isPresent()) {
                FlightShopResponse flightShopResponse = optResponse.get();

                if (!flightShopResponse.isEmpty()) {
                    try {
                        FlightFees flightFees = results.get();
                        // todo filter the fees to only include results for flights that we have here
                        flightShopResponse.setFlightFees(flightFees);
                    } catch (InterruptedException e) {
                        logger.warn("failure getting fees from future", e);
                    } catch (ExecutionException e) {
                        logger.warn("failure getting fees from future", e);
                        if (e.getCause() instanceof SupportingServiceException) {
                            handleSupportingServiceException("g4", (SupportingServiceException) e.getCause(),
                                    flightShopResponse);
                        }
                    }
                }
            }
        } catch (MissingAncillaryFeesException e) {
            logger.warn("Missing one or more responses from ancillary service");
            ProviderFlightShopResponse response = new ProviderFlightShopResponse(new FlightShopResponse());
            FilterUtil.clearOutput(exchange);
            exchange.addOut(response);
        }
    }
}
