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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Generated;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Walks the aggregated list of flights and removes any flights from other providers where G4 has a flight on the same
 * day and in the same market
 */
@Named
public class RemoveCompetingFlightsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RemoveCompetingFlightsFilter.class);

    @Override
    @Profile
    public void process(Exchange exchange) {
        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);
        assert outputs.size() == 1;

        ProviderFlightShopResponse response = outputs.get(0);
        FlightShopResponse flightShopResponse = response.getResponse();
        if (!flightShopResponse.isEmpty()) {
            process(flightShopResponse);
        }
    }

    void process(FlightShopResponse flightShopResponse) {
        logger.trace("running");
        List<FlightOption> removed = new ArrayList<>();

        for (FlightOptions options : flightShopResponse.getFlightOptions().values()) {
            removed.addAll(removeCompetingFlights(options));
        }

        // convert the removed to their package identifiers and then remove all references to those packages
        if (!removed.isEmpty()) {
            logger.debug("removed {} flights, checking for package removal", removed.size());
            Set<PackageId> packagesToRemove = new HashSet<>();

            // identify all of the packages that we're removing
            for (FlightOption option : removed) {
                for (ProviderFares ppo : option.getProviderFares()) {
                    if (!ppo.getPackageId().isNone()) {
                        packagesToRemove.add(ppo.getPackageId());
                    }
                }
            }

            logger.trace("scanning to remove all of the following packages {}", packagesToRemove);

            // walk the flights again and remove all of the packages
            for (FlightOptions options : flightShopResponse.getFlightOptions().values()) {
                for (FlightOption option : options.getFlightOptions()) {
                    Predicate<ProviderFares> removeId = ppo -> {
                        boolean remove = packagesToRemove.contains(ppo.getPackageId());
                        if (remove) {
                            logger.trace("removed package {} from option {}",  ppo.getPackageId(), option);
                        }
                        return remove;
                    };
                    option.removePackage(removeId);
                }
                options.getFlightOptions().removeIf(o -> o.getProviderFares().isEmpty());
            }
        }
    }

    /**
     * Removes any competing flights from the options passed in
     * @param flightOptions - any competing flights are removed from the options passed in
     * @return the list of removed options or an empty list if none removed
     */
    List<FlightOption> removeCompetingFlights(FlightOptions flightOptions) {

        List<FlightOption> retVal = new ArrayList<>();

        // gather all of the g4 flight options
        Set<CompetingFlightInfo> g4CompetingFlightInfo = new HashSet<>();
        List<FlightOption> nonCompeting = new ArrayList<>();
        List<FlightOption> flightsFromOtherCarriersToCheck = new ArrayList<>();

        for (FlightOption option : flightOptions.getFlightOptions()) {
            AirlineCode carrierCode = option.getSegments().get(0).getCarrierCode();
            if (AirlineCode.G4.equals(carrierCode)) {
                // it's a g4 flight
                nonCompeting.add(option);
                g4CompetingFlightInfo.add(new CompetingFlightInfo(option));
            } else {
                flightsFromOtherCarriersToCheck.add(option);
            }
        }

        // now check all of the flights from the other carriers to see if they conflict with a g4 flight
        for (FlightOption option : flightsFromOtherCarriersToCheck) {
            CompetingFlightInfo possibleCompetition = new CompetingFlightInfo(option);
            if (g4CompetingFlightInfo.contains(possibleCompetition)) {
                retVal.add(option);
            } else {
                nonCompeting.add(option);
            }
        }

        flightOptions.setFlightOptions(nonCompeting);

        return retVal;
    }

    private static class CompetingFlightInfo {
        private final AirportCode origin;
        private final LocalDate departureDate;

        CompetingFlightInfo(AirportCode origin, LocalDate departureDate) {
            this.origin = origin;
            this.departureDate = departureDate;
        }

        CompetingFlightInfo(FlightOption option) {
            this(option.getFirstOrigin(),
                    option.getSegments().get(0).getLegs().get(0).getDepartureTime().toLocalDate());
        }

        @Generated("by IDE")
        public AirportCode getOrigin() {
            return origin;
        }

        @Generated("by IDE")
        public LocalDate getDepartureDate() {
            return departureDate;
        }

        @Override
        @Generated("by IDE")
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CompetingFlightInfo that = (CompetingFlightInfo) o;
            return Objects.equals(origin, that.origin) &&
                    Objects.equals(departureDate, that.departureDate);
        }

        @Override
        @Generated("by IDE")
        public int hashCode() {
            return Objects.hash(origin, departureDate);
        }
    }
}
