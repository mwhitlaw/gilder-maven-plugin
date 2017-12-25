/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.provider;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class LookupProducer {

    private final AircraftLookup aircraftLookup;
    private final AirportLookup airportLookup;
    private final AirlineLookup airlineLookup;
    private final NamedFareDetailsLookup namedFareDetailsLookup;

    public LookupProducer() {
        List<AircraftLookup> aircraftLookups = lookup(AircraftLookup.class);
        aircraftLookup = key -> doLookup(aircraftLookups, key);

        List<AirportLookup> airportLookups = lookup(AirportLookup.class);
        airportLookup = key -> doLookup(airportLookups, key);

        List<AirlineLookup> airlineLookups = lookup(AirlineLookup.class);
        airlineLookup = key -> doLookup(airlineLookups, key);

        List<NamedFareDetailsLookup> namedFareDetailsLookups = lookup(NamedFareDetailsLookup.class);
        namedFareDetailsLookup = key -> doLookup(namedFareDetailsLookups, key);
    }

    @Produces
    @AggregatedProviderLookup
    public AircraftLookup aircraft() {
        return aircraftLookup;
    }

    @Produces
    @AggregatedProviderLookup
    public AirportLookup airport() {
        return airportLookup;
    }

    @Produces
    @AggregatedProviderLookup
    public AirlineLookup airline() {
        return airlineLookup;
    }

    @Produces
    @AggregatedProviderLookup
    public NamedFareDetailsLookup specialFares() {
        return namedFareDetailsLookup;
    }

    private <T> List<T> lookup(Class<T> type) {
        ServiceLoader<T> loader = ServiceLoader.load(type);
        return StreamSupport.stream(loader.spliterator(), false)
                .collect(Collectors.toList());
    }

    private <K, V> V doLookup(List<? extends LookupService<K, V>> fromProviders, K key) {
        return fromProviders
                .stream()
                .map(lookup -> lookup.lookup(key))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}



