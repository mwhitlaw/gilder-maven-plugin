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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightKey;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MergeFlightOptions {

    private static final Logger logger = LoggerFactory.getLogger(MergeFlightOptions.class);

    private MergeFlightOptions() {}

    static FlightOptions merge(List<FlightOptions> list) {
        return merge(list.stream());
    }

    static FlightOptions merge(Stream<FlightOptions> stream) {

        Map<List<FlightKey>, FlightOption> map = new HashMap<>();

        FlightOptions merged = new FlightOptions();
        ArrayList<FlightOption> mergedOptions = new ArrayList<>();
        merged.setFlightOptions(mergedOptions);

        stream.forEach(options -> {
            logger.debug("merging output from {} options", options.getFlightOptions().size());
            for (FlightOption option : options.getFlightOptions()) {
                List<FlightKey> keys = option.getSegmentKeys();
                FlightOption existing = map.get(keys);

                if (existing == null) {
                    map.put(keys, option);
                    mergedOptions.add(option);
                    // ensures that the provider list is mutable
                    option.setProviderFares(new ArrayList<>(option.getProviderFares()));
                } else {
                    existing.getProviderFares().addAll(option.getProviderFares());
                }
            }
        });
        return merged;
    }

    /**
     * A FlightOption is considered a dupe if all of the flights in its package already exist in the
     * FlightOptions object. If this is the case, we'll keep whichever of the two sets of flights
     * are cheaper.
     *
     * Example: Suppose we have QPX and Sabre as providers for servicing a BOS-LAS round trip flight.
     * It's likely that they would produce some duplicate items. Perhaps the direct BOS-LAS flight
     * from Spirit and the corresponding red-eye back.
     *
     * Sabre: BOS-LAS NK-641 $258.38 package-A
     * QPX:   BOS-LAS NK-641 $258.38 package-B
     *
     * Sabre: LAS-BOS NK-123 $0.00 package-A
     * QPX:   LAS-BOS NK-123 $0.00 package-B
     *
     * Given the above, the dupe removed would be whichever is more expensive. If the prices are the
     * same (as shown) then we'd either have a preference set for which provider to keep or perhaps
     * just keep the first in order in the FlightOptions.
     *
     * @throws Exception
     */
    static void removeDupes(Map<FlightRequest, FlightOptions> groupedByRequest) {

        // maps a package id to all of the flight options that reference the package id
        Map<PackageId, AllFlightsInPackage.Builder> flightKeysByPackageId = new LinkedHashMap<>();

        // walk all of the flight options and build a map for flights with packaged pricing keyed by the PackageId
        groupedByRequest.values()
                .stream()
                .flatMap(fo -> fo.getFlightOptions().stream())
                .forEach(option -> {
            for (ProviderFares pricingOption : option.getProviderFares()) {
                PackageId packageId = pricingOption.getPackageId();
                // we only care about prices tied to packages
                if (!packageId.isNone()) {
                    AllFlightsInPackage.Builder allFlights = flightKeysByPackageId.computeIfAbsent(
                            packageId,
                            AllFlightsInPackage::builder);
                    allFlights.add(option);
                }
            }
        });

        // keep track of all of the pricing packages that we want to remove. The goal is to avoid
        // showing the consumer multiple options for the same flight keys from different providers.
        // We should just show them the cheapest one. For now, we'll keep whichever we come across first
        List<AllFlightsInPackage> toBeRemoved = new ArrayList<>();

        Map<AllFlightsInPackage, AllFlightsInPackage> packageIdsByFlights = new LinkedHashMap<>();
        for (Map.Entry<PackageId, AllFlightsInPackage.Builder> entry : flightKeysByPackageId.entrySet()) {
            AllFlightsInPackage build = entry.getValue().build();
            AllFlightsInPackage existing = packageIdsByFlights.putIfAbsent(build, build);
            if (existing != null) {
                // here's where we should determine which package to keep based on sale price or other logic
                toBeRemoved.add(build);
            }
        }

        // Walk all of the packages we want to remove and remove the pricing option tied to that package
        // on all of the flights where it's referenced.
        for (AllFlightsInPackage allFlightsInPackage : toBeRemoved) {
            PackageId removeAllPackagesWithThisId = allFlightsInPackage.getPackageId();

            for (FlightOption flightOption : allFlightsInPackage.getFlightOptions()) {
                flightOption.setProviderFares(
                        flightOption.getProviderFares()
                                .stream()
                                .filter(pricing -> !pricing.getPackageId().equals(removeAllPackagesWithThisId))
                                .collect(Collectors.toList())
                );
            }
        }
    }


    /**
     * Groups the packageId along with all of the flights it is referenced in as well as the FlightKey
     * objects for every segment. This class is suitable for use as a key in a Map since its hashcode
     * and equals are based only on the FlightKeys so we can detect two identical packages.
     */
    private static final class AllFlightsInPackage {
        private final PackageId packageId;
        private final List<FlightKey> flights;
        private final List<FlightOption> flightOptions;

        private AllFlightsInPackage(Builder builder) {
            this.flights = Collections.unmodifiableList(builder.flights);
            this.flightOptions = Collections.unmodifiableList(builder.flightOptions);
            this.packageId = builder.packageId;
        }

        public static Builder builder(PackageId packageId) {
            return new Builder(packageId);
        }

        @Generated("Generated by IDE")
        public PackageId getPackageId() {
            return packageId;
        }

        @Generated("Generated by IDE")
        public List<FlightKey> getFlights() {
            return flights;
        }

        @Generated("Generated by IDE")
        public List<FlightOption> getFlightOptions() {
            return flightOptions;
        }

        @Override
        @Generated("by IDE")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AllFlightsInPackage that = (AllFlightsInPackage) o;

            return flights.equals(that.flights);
        }

        @Override
        @Generated("by IDE")
        public int hashCode() {
            return flights.hashCode();
        }

        /**
         * A Builder for the AllFlightsInPackage class. The builder is required because we're gradually
         * building all of the references from the PackageId as we traverse the flights and we don't want
         * to have the AllFlightsInPackage class be mutable since it's used as a key so its hashcode and
         * equals should be stable.
         */
        static class Builder {
            private final List<FlightKey> flights = new ArrayList<>();
            private final List<FlightOption> flightOptions = new ArrayList<>();
            private final PackageId packageId;

            Builder(PackageId packageId) {
                this.packageId = packageId;
            }

            void add(FlightOption flightOption) {
                this.flightOptions.add(flightOption);
                this.flights.addAll(flightOption.getSegmentKeys());
            }

            AllFlightsInPackage build() {
                return new AllFlightsInPackage(this);
            }
        }
    }
}
