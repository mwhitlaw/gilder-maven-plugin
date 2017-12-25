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

import com.allegianttravel.ota.flight.shop.provider.AircraftLookup;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Aircraft;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AircraftCode;
import com.allegianttravel.ota.flight.shop.sabre.SabreApplicationException;
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

public class SabreAircraftLookup implements AircraftLookup {

    private static final Logger logger = LoggerFactory.getLogger(SabreAircraftLookup.class);

    private final Map<AircraftCode, Aircraft> cache;

    public SabreAircraftLookup() {
        AircraftCacheLoader loader = new AircraftCacheLoader();
        ExpiringMap<AircraftCode, Aircraft> expiringMap = ExpiringMap.builder()
                .expiration(1, TimeUnit.HOURS)
                .entryLoader(loader)
                .build();
        expiringMap.addAsyncExpirationListener((key, value) -> loader.load(key));
        cache = expiringMap;
    }

    @Override
    public Aircraft lookup(AircraftCode key) {
        return cache.get(key);
    }

    public static class AircraftCacheLoader implements EntryLoader<AircraftCode, Aircraft> {

        @Override
        public Aircraft load(@Nonnull AircraftCode key) {

            String endpoint = SabreProperties.ENDPOINT.makeEndpoint("/");
            SabreAircraftService aircraftService = SabreRetrofitClient.createSabreServiceProxy(endpoint,
                    SabreAircraftService.class);

            Call<SabreAircraftResponse> call = aircraftService.getAircraft(key);
            Aircraft retVal;

            try {
                SabreAircraftResponse sabreAircraftResponse = SabreRetrofitClient.callSabreWithAuthRetry(call);
                // sabre-demo: not a great mapping. Should drop the Sabre implementation in favor of G4's db
                SabreAircraftResponse.AircraftEntry aircraftEntry = sabreAircraftResponse.getAircraftInfo().get(0);
                retVal = new Aircraft().setCode(aircraftEntry.getAircraftCode());
                String aircraftName = aircraftEntry.getAircraftName();
                retVal.setMake(aircraftName);
                retVal.setModel(aircraftName);
            } catch (SabreApplicationException | SabreAuthException e) {
                logger.debug("failed to get aircraft {}", key, e);
                retVal = null;
            }
            return retVal;
        }
    }
}
