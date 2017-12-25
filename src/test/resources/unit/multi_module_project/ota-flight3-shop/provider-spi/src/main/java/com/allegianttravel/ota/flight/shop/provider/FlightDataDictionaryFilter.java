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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightDataDictionary;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.NamedFareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Aircraft;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airline;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airport;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Named
public class FlightDataDictionaryFilter implements Filter {

    private AirlineLookup airlineLookup;
    private AircraftLookup aircraftLookup;
    private AirportLookup airportLookup;
    private NamedFareDetailsLookup namedFareDetailsLookup;

    @Inject
    public FlightDataDictionaryFilter(@AggregatedProviderLookup AirlineLookup airlineLookup,
                                      @AggregatedProviderLookup AircraftLookup aircraftLookup,
                                      @AggregatedProviderLookup AirportLookup airportLookup,
                                      @AggregatedProviderLookup NamedFareDetailsLookup namedFareDetailsLookup) {
        this.aircraftLookup = aircraftLookup;
        this.airlineLookup = airlineLookup;
        this.airportLookup = airportLookup;
        this.namedFareDetailsLookup = namedFareDetailsLookup;
    }

    @Override
    @Profile
    public void process(Exchange exchange) {
        Optional<FlightShopResponse> optResponse = FilterUtil.getResponse(exchange);

        if (optResponse.isPresent()) {
            FlightShopResponse flightShopResponse = optResponse.get();
            if (!flightShopResponse.isEmpty()) {

                final FlightDataDictionary dictionary = new FlightDataDictionary();

                flightShopResponse.getFlightOptions().values()
                        .stream()
                        .flatMap(fos -> fos.getFlightOptions().stream())
                        .forEach(fo -> traverse(fo, dictionary));

                flightShopResponse.setDataDictionary(dictionary);
            }
        }
    }

    private void traverse(FlightOption flightOption, FlightDataDictionary dictionary) {

        // add the info about the special fares
        addSpecialFares(flightOption, dictionary);

        for (Segment segment : flightOption.getSegments()) {
            AirlineCode carrierCode = segment.getCarrierCode();
            AirlineCode operatorCode = segment.getOperatorCode();

            addAirline(carrierCode, dictionary);
            addAirline(operatorCode, dictionary);

            for (Leg leg : segment.getLegs()) {
                addAircraft(leg, dictionary);
                addAirport(leg.getDestination(), dictionary);
                addAirport(leg.getOrigin(), dictionary);
            }
        }
    }

    private void addSpecialFares(FlightOption flightOption, FlightDataDictionary dictionary) {
        flightOption.getProviderFares().stream().flatMap(pf -> pf.getNamedFares().stream())
                .forEach(namedFare -> {
            NamedFareDetails details = namedFareDetailsLookup.lookup(namedFare);
            if (details != null) {
                dictionary.getNamedFareDetails().put(namedFare.getDictionaryKey(), details);
            }
        });
    }

    private void addAirport(AirportCode airportCode, FlightDataDictionary dictionary) {
        if (airportCode != null) {
            Airport value = airportLookup.lookup(airportCode);
            if (value != null) {
                dictionary.getAirports().put(airportCode, value);
            }
        }
    }

    private void addAircraft(Leg leg, FlightDataDictionary dictionary) {

        if (leg.getAircraftMake() != null || leg.getAircraftModel() != null) {
            // use the private values from the leg. They may have been set by the provider if the aircraft code is
            // something exotic.
            dictionary.getAircraft().computeIfAbsent(leg.getAircraftCode(),
                    key -> new Aircraft()
                            .setCode(key)
                            .setMake(leg.getAircraftMake())
                            .setModel(leg.getAircraftModel()));
        } else if (leg.getAircraftCode() != null) {
            Aircraft value = aircraftLookup.lookup(leg.getAircraftCode());
            if (value != null) {
                dictionary.getAircraft().putIfAbsent(leg.getAircraftCode(), value);
            }
        }
    }

    private void addAirline(AirlineCode airlineCode, FlightDataDictionary dictionary) {
        if (airlineCode != null) {
            Airline value = airlineLookup.lookup(airlineCode);
            if (value != null) {
                dictionary.getAirline().put(airlineCode, value);
            }
        }
    }
}
