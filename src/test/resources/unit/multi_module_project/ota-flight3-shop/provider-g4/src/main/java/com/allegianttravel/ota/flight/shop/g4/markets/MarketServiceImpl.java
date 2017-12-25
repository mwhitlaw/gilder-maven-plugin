/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.markets;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiant.ota.markets.model.MarketList;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight3.MarketName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

public class MarketServiceImpl implements MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketServiceImpl.class);

    @Override
    public boolean isAllowed(MarketName marketName) {

        G4MarketsClient g4MarketsClient = ServiceGenerator.create(G4MarketsClient.class,
                G4FlightProperties.G4_OTA_MAINT_ENDPOINT.makeEndpoint("/"));

        Call<MarketList> call = g4MarketsClient.getMarkets(marketName);
        try {
            Response<MarketList> response = call.execute();
            boolean allowed = response.isSuccessful();
            if (!allowed) {
                logger.debug("market {} is not allowed. Satus code {} Payload: {}", marketName.getName(),
                        response.code(), response.errorBody().string());
            }
            return allowed;
        } catch (Exception e) {
            logger.error("market lookup for {} resulted in an error on {}",
                    marketName.getName(), call.request().url().toString(), e);
            return false;
        }
    }
}
