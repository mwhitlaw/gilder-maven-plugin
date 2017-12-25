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
import com.allegianttravel.ota.flight.shop.provider.AbstractFlightShopRequestProviderInput;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequestProviderInput;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.List;
import java.util.concurrent.Future;

@Named
public class G4FareMakerOutboundFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(G4FareMakerOutboundFilter.class);

    @Override
    @Profile
    public void process(Exchange exchange) {
        @SuppressWarnings("unchecked") Future<FareMakerResults> future = (Future<FareMakerResults>)
                exchange.getProperty(G4FareMakerAsyncFilter.FUTURE_KEY);

        if (future != null) {
            FareMakerResults results = null;
            try {
                results = future.get();
            } catch (Exception e) {
                logger.warn("Error getting FareMakerResults", e);
            }

            if (results != null) {
                decorateG4Flights(exchange, results);
            }
        }
    }

    private void decorateG4Flights(Exchange exchange, FareMakerResults results) {

        FlightShopRequestProviderInput input = exchange.getIn(AbstractFlightShopRequestProviderInput.class);

        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);
        assert outputs.size() == 1;

        ProviderFlightShopResponse response = outputs.get(0);
        G4FareMakerUtil.decorateG4Flights(results, response, input);
    }

}
