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

import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.provider.NamedFareDetailsLookup;
import com.allegianttravel.ota.flight.shop.rest.dto.BundledFareItemSummary;
import com.allegianttravel.ota.flight.shop.rest.dto.NamedFareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsible for adding summary information and swimlane hints to the fares
 */
@ApplicationScoped
public class G4NamedFareDetailsLookup implements NamedFareDetailsLookup {

    private static final Logger logger = LoggerFactory.getLogger(G4NamedFareDetailsLookup.class);

    /**
     * map of fare names to their swim lane position
     */
    private Map<String, Integer> swimlanesMap;

    /**
     * the next time we should load the swim lane map. Don't want to cache forever
     */
    private ZonedDateTime nextLoadTime;

    /**
     * Initializes the swimlane map based on the system property
     */
    public G4NamedFareDetailsLookup() {
        updateMapAndTime();
    }

    @Override
    public NamedFareDetails lookup(NamedFare namedFare) {
        Map<String, Integer> map = getLatestMap();
        return new NamedFareDetails()
                .setSwimlane(toSwimlane(map, namedFare.getName()))
                .setItems(namedFare.getFareDetails()
                        .stream()
                        .filter(fareDetails -> fareDetails.getBundledItems() != null)
                        .flatMap(fareDetails -> fareDetails.getBundledItems().stream())
                        .map(bundledFareItem -> new BundledFareItemSummary()
                                .setItemType(bundledFareItem.getItemType())
                        )
                        .collect(Collectors.toList()));
    }

    /**
     * returns the swimlane for the given fare. If it's not found in the map, then we'll fallback
     * to "2" which is admittedly an arbitrary choice.
     *
     * @param swimlanesMap
     * @param name
     */
    static int toSwimlane(Map<String, Integer> swimlanesMap, String name) {
        Integer lane = swimlanesMap.get(name);
        if (lane == null) {
            logger.debug("No lane found for {}, falling back to 2", name);
            lane = 2;
        }
        return lane;
    }

    /**
     * Converts a single line of input with comma separated name/value pairs into a map
     * of string to int. Each pair is the name of the fare and its swimlane position.
     *
     * Example:
     *
     * STD=1,FLEX=2
     *
     * The format allows for whitespace by doing a trim on the key/value but any other
     * issues will result in the en
     *
     * @param input
     * @return
     */
    static Map<String, Integer> toMap(String input) {
        String nonNullInput = StringUtils.defaultIfBlank(input, "");
        if (nonNullInput.isEmpty()) {
            logger.debug("input from System Property is empty, using default map");
            return createDefaultMap();
        }

        // it's not empty, split it
        Map<String, Integer> map = new LinkedHashMap<>();
        String[] split = nonNullInput.split(",");
        for (String s : split) {
            String[] entry = s.split("=");
            if (entry.length == 2) {
                try {
                    map.put(entry[0].trim(),
                            Integer.valueOf(entry[1].trim()));
                } catch (NumberFormatException e) {
                    logger.warn("Ignoring malformed entry {} for swimlane. NAME=1", s);
                }
            } else {
                logger.warn("Ignoring malformed entry {}", s);
            }
        }

        if (map.isEmpty()) {
            logger.debug("No entries added from input {}, falling back to default map", input);
            return createDefaultMap();
        }

        return map;
    }

    private static Map<String, Integer> createDefaultMap() {
        return Collections.singletonMap(ProviderFares.STD_RATE, 1);
    }

    private void updateMapAndTime() {
        nextLoadTime = ZonedDateTime.now().plusMinutes(getCacheTimeInMinutes());
        swimlanesMap = toMap(G4FlightProperties.G4_SWIM_LANES.getValue());
    }

    private Map<String, Integer> getLatestMap() {
        if (ZonedDateTime.now().isAfter(nextLoadTime)) {
            updateMapAndTime();
        }
        return swimlanesMap;
    }

    private static int getCacheTimeInMinutes() {
        String value = G4FlightProperties.G4_SWIM_LANES_CACHE.getValue();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Error parsing cache default time in minutes for swimlanes map: {}", value);
            return 10;
        }
    }

}
