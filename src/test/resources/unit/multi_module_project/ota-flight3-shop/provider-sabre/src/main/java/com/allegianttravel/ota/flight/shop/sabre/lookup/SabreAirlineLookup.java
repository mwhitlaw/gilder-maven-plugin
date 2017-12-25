/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.sabre.lookup;

import com.allegianttravel.ota.flight.shop.provider.AirlineLookup;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airline;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.sabre.SabreAuthException;
import com.allegianttravel.ota.flight.shop.sabre.SabreProperties;
import com.allegianttravel.ota.flight.shop.sabre.SabreRetrofitClient;
import net.jodah.expiringmap.EntryLoader;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SabreAirlineLookup implements AirlineLookup {
    private static final Logger logger = LoggerFactory.getLogger(SabreAirlineLookup.class);

    // sabre-demo: remove this from the server. Have the UI hard code this since it's demo only and Sabre won't ship
    // not all airlines will be here (ie: g4)
    // for the time being, ui handle special cases
    private static final String HREF_TEMPLATE =
            "https://images.trvl-media.com/media/content/expus/graphics/static_content/" +
                    "fusion/v0.1b/images/airlines/vector/s/%s_sq.svg";

    private final Map<AirlineCode, Airline> cache;


    public SabreAirlineLookup() {
        AirlineCacheLoader loader = new AirlineCacheLoader();
        ExpiringMap<AirlineCode, Airline> expiringMap = ExpiringMap.builder()
                .expiration(1, TimeUnit.HOURS)
                .entryLoader(loader)
                .build();
        expiringMap.addAsyncExpirationListener((key, value) -> loader.load(key));
        cache = expiringMap;

    }

    @Override
    public Airline lookup(AirlineCode key) {
        return cache.get(key);
    }

    public static class AirlineCacheLoader implements EntryLoader<AirlineCode, Airline> {

        @Override
        public Airline load(@Nonnull AirlineCode key) {

            String endpoint = SabreProperties.ENDPOINT.makeEndpoint("/");

            SabreAirlineService airlineService = SabreRetrofitClient.createSabreServiceProxy(endpoint,
                    SabreAirlineService.class);

            Call<SabreAirlineResponse> call = airlineService.getAirline(key);
            Airline retVal;

            try {
                SabreAirlineResponse sabreAirlineResponse = SabreRetrofitClient.callSabreWithAuthRetry(call);
                retVal = new Airline()
                        .setAirlineCode(key)
                        .setIconHref(String.format(HREF_TEMPLATE, key.getValue()))
                        .setName(sabreAirlineResponse.getAirlineInfo().get(0).getAirlineName());
            } catch (SabreAuthException e) {
                logger.debug("failed to get airline {}", key, e);
                retVal = null;
            }

            return retVal;
        }
    }
}
