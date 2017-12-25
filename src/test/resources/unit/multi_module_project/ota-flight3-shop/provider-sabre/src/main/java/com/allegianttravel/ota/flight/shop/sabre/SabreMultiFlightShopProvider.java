/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.sabre;

import com.allegianttravel.ota.flight.shop.provider.MultiCityFlightSearchCall;
import com.allegianttravel.ota.flight.shop.provider.PackageSupplier;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.provider.ProviderMultiCityShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.MultiCityShopRequest;
import com.allegianttravel.ota.framework.module.spi.ManagedExecutorService;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import com.allegianttravel.sabre.generated.DestinationLocation;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQ;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRS;
import com.allegianttravel.sabre.generated.OriginDestinationInformation;
import com.allegianttravel.sabre.generated.OriginLocation;
import com.allegianttravel.sabre.generated.POS;
import com.allegianttravel.sabre.generated.TPAExtensions_____;
import com.allegianttravel.sabre.generated.TravelerInfoSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.allegianttravel.ota.flight.shop.sabre.SabreRetrofitClient.search;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.buildFlexibilityExtensions;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.createIntelliSellTx;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.createPOS;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.createTravelerPreferences;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.removeFakedOutRoundTripInboundResults;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.toTravelerInfoSummary;

@Named
@SuppressWarnings("unused")
public class SabreMultiFlightShopProvider implements Provider {

    private static final Logger logger = LoggerFactory.getLogger(SabreMultiFlightShopProvider.class);

    /**
     * Used to execute the REST calls in parallel
     */
    private ManagedExecutorService executorService;

    @Inject
    public SabreMultiFlightShopProvider(ManagedExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Required by CDI
     */
    public SabreMultiFlightShopProvider() {
    }


    @Override
    public String getName() {
        return "sabre-multi";
    }

    @Override
    public String getDescription() {
        return "Sabre-multi REST";
    }

    @Override
    @Profile
    public ProviderOutput provide(ProviderInput input) {

        PackageSupplier packageSupplier = new PackageSupplier(getName());

        ProviderMultiCityShopRequest providerMultiCityShopRequest = (ProviderMultiCityShopRequest) input;
        MultiCityShopRequest multiCityShopRequest = providerMultiCityShopRequest.getMultiCityShopRequest();

        logger.debug("servicing multi city request {}", multiCityShopRequest);


        final TravelerInfoSummary travelerInfoSummary = toTravelerInfoSummary(multiCityShopRequest.getPassengers());

        final TPAExtensions_____ intelliSellTx = createIntelliSellTx();

        final POS pos = createPOS();

        List<Callable<MultiCityFlightSearchCall>> callables = multiCityShopRequest.getFlights()
                .stream()
                .map(fr -> (Callable<MultiCityFlightSearchCall>) () -> {
                    OTAAirLowFareSearchRQ searchRQ = new OTAAirLowFareSearchRQ()
                            .withOriginDestinationInformation(Arrays.asList(
                                    new OriginDestinationInformation()
                                            .withDepartureDateTime(fr.getTravelDate().atStartOfDay())
                                            .withTPAExtensions(buildFlexibilityExtensions(fr))
                                            .withOriginLocation(new OriginLocation()
                                                    .withLocationCode(fr.getOrigin().getValue()))
                                            .withDestinationLocation(new DestinationLocation()
                                                    .withLocationCode(fr.getDestination().getValue())),
                                    // sabre-demo - THIS IS A WORKAROUND FOR LIMITATIONS IN THE SABRE TEST PLATFORM
                                    // The test platform doesn't seem to have one-way fares published.
                                    // To work around this, I'll search for a round trip
                                    new OriginDestinationInformation()
                                            .withDepartureDateTime(fr.getTravelDate().plusDays(7).atStartOfDay())
                                            .withOriginLocation(new OriginLocation()
                                                    .withLocationCode(fr.getDestination().getValue()))
                                            .withDestinationLocation(new DestinationLocation()
                                                    .withLocationCode(fr.getOrigin().getValue())))

                                    )
                            .withPOS(pos)
                            .withTPAExtensions(intelliSellTx)
                            .withTravelPreferences(createTravelerPreferences(fr, multiCityShopRequest.getMaxResults()))
                            .withTravelerInfoSummary(travelerInfoSummary);

                    try {
                        Optional<OTAAirLowFareSearchRS> otaAirLowFareSearchRS = search(searchRQ);
                        if (otaAirLowFareSearchRS.isPresent()) {
                            FlightOptions flightOptions = new SabreToFlightOptionMapper(otaAirLowFareSearchRS.get(),
                                    packageSupplier).map();
                            return new MultiCityFlightSearchCall(fr, flightOptions);
                        } else {
                            return new MultiCityFlightSearchCall(fr, new FlightOptions());
                        }
                    } catch (SabreApplicationException e) {
                        return new MultiCityFlightSearchCall(fr, new FlightOptions().setFlightOptions(
                                Collections.emptyList()));
                    }
                })
                .collect(Collectors.toList());

        logger.debug("prepared {} sabre calls", callables.size());

        List<MultiCityFlightSearchCall> results = join(fork(callables));

        Map<FlightRequest, FlightOptions> optionsByRequest = new LinkedHashMap<>();
        for (MultiCityFlightSearchCall call : results) {
            optionsByRequest.put(call.getFlightRequest(), call.getFlightOptions());
        }

        logger.debug("grouped responses prepared for {}", optionsByRequest.keySet());

        FlightShopResponse response = new FlightShopResponse();
        response.setFlightOptions(optionsByRequest);

        return new ProviderFlightShopResponse(response);
    }

    /**
     * Gets the result of the search and returns the list of search calls with their request/response details
     * @param futures
     * @return
     */
    private List<MultiCityFlightSearchCall> join(List<Future<MultiCityFlightSearchCall>> futures) {
        // walk the callables and grab each FlightOptions result but prune/adjust the
        // flights within it to remove the inbound flight
        List<MultiCityFlightSearchCall> result = new ArrayList<>();
        try {
            for (Future<MultiCityFlightSearchCall> future : futures) {
                MultiCityFlightSearchCall call = future.get();
                FlightOptions flightOptions = call.getFlightOptions();
                AirportCode origin = call.getFlightRequest().getOrigin();
                removeFakedOutRoundTripInboundResults(flightOptions, origin);
                result.add(call);
            }
        } catch (Exception e) {
            logger.error("Error invoking sabre", e);
        }
        return result;
    }

    private List<Future<MultiCityFlightSearchCall>> fork(List<Callable<MultiCityFlightSearchCall>> callables) {
        return callables.stream().map(executorService::submit).collect(Collectors.toList());
    }


    @Override
    public int clearCaches() {
        return 0;
    }
}
