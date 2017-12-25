/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.lookup;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiantair.airport.dto.AirportDTO;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.provider.AirportLookup;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airport;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import net.jodah.expiringmap.EntryLoader;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Looks up an Airport object from an AirportCode using the G4 service (which pulls from CMSDB)
 */
public class G4AirportLookup implements AirportLookup {
    private static final Logger logger = LoggerFactory.getLogger(G4AirportLookup.class);

    private final Map<AirportCode, Airport> cache;

    @SuppressWarnings("WeakerAccess")
    public G4AirportLookup() {
        AirportLookupCacheLoader loader = new AirportLookupCacheLoader();
        ExpiringMap<AirportCode, Airport> expiringMap = ExpiringMap.builder()
                .expiration(1, TimeUnit.HOURS)
                .entryLoader(loader)
                .build();
        expiringMap.addAsyncExpirationListener((key, value) -> expiringMap.put(key, loader.load(key)));
        cache = expiringMap;
    }

    @Override
    public Airport lookup(AirportCode key) {
        return cache.get(key);
    }

    static class AirportLookupCacheLoader implements EntryLoader<AirportCode, Airport> {

        @Override
        public Airport load(@Nonnull AirportCode key) {

            // switched from the proxy style invoke because of some weirdness with
            // the rest easy library unmarshalling the payload.
            // this problem appeared when redeploying the war during testing
            String endpoint = G4FlightProperties.G4_AIRPORTS.makeEndpoint("/");
            G4AirportClient g4AirportClient = ServiceGenerator.create(G4AirportClient.class, endpoint);
            Call<List<AirportDTO>> call = g4AirportClient.getAirports(key);
            Airport retVal;
            try {
                Response<List<AirportDTO>> response = call.execute();

                if (response.isSuccessful()) {
                    List<AirportDTO> results = response.body();
                    if (results.isEmpty()) {
                        logger.debug("Airport {} not found in G4 db", key);
                        retVal = new Airport().setAirportCode(key).setName(key.toString());
                    } else {
                        AirportDTO airportDTO = results.get(0);
                        retVal = new Airport()
                                .setAirportCode(key)
                                .setName(airportDTO.getCity())
                                .setZoneId(TimeZone.getTimeZone(airportDTO.getTimeZone()).toZoneId().toString())
                                .setId(airportDTO.getRecId())
                                .setCity(airportDTO.getCity())
                                .setState(airportDTO.getState())
                        .setAllowEBoardingPass("Y".equals(airportDTO.getIsScanner()));
                    }
                } else {
                    logger.warn("error looking up Airport {} at {}. Payload: {}",
                            key, call.request().url(), response.errorBody().string());
                    retVal = null;
                }
            } catch (Exception e) {
                logger.warn("error looking up Airport {} at {}", key, call.request().url(), e);
                retVal = null;
            }
            return retVal;
        }
    }
}
