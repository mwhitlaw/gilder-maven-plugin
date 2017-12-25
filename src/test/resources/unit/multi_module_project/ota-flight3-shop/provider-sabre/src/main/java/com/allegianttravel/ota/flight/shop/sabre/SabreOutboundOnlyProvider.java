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

import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.PackageSupplier;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Collections;

import static com.allegianttravel.ota.flight.shop.provider.FlightShopUtils.toProviderResponse;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.removeFakedOutRoundTripInboundResults;

/**
 * Handles both one-way and round trip requests.
 *
 * In the case of one-way, we'll search for a round trip and fake out the results since
 * the sabre test platform doesn't support one way.
 *
 * In the case of round trip, we'll do the same search as one-way because we want to
 * offer adhoc round trip packages.
 */
@Named
public class SabreOutboundOnlyProvider extends AbstractSabreProvider {

    private static final Logger logger = LoggerFactory.getLogger(SabreOutboundOnlyProvider.class);

    @Profile
    @Override
    public ProviderOutput provide(ProviderInput input) {
        FlightShopRequest shopRequest = (FlightShopRequest) input;

        if (SabreUtils.isAllowed(shopRequest)) {
            try {
                logger.debug("servicing request for sabre {}", shopRequest);

                FlightShopRequest searchRequest = shopRequest.getReturnDate() == null ? FlightShopRequest
                        .builder(shopRequest)
                        .setReturnDate(shopRequest.getDepartDate().plusDays(7))
                        .build() : shopRequest;

                PackageSupplier packageSupplier = new PackageSupplier(getName());

                FlightOptions flightOptions = searchRoundTrip(searchRequest, packageSupplier);

                // remove all of the return flights
                // set the package to NONE on each of the outbound flights
                // cut the prices in half for the demo to approximate one-way
                removeFakedOutRoundTripInboundResults(flightOptions, shopRequest.getDepartAirportCode());

                logger.debug("sabre responding with {} options", flightOptions.getFlightOptions().size());
                return toProviderResponse(shopRequest, flightOptions);
            } catch (IOException e) {
                return new ProviderFlightShopResponse(new FlightShopResponse().setErrorMessages(
                        Collections.singletonList(new ErrorMessage()
                                .setSeverity(Severity.WARNING)
                                .setProviderId("sabre")
                                .setMessage(e.getMessage())))
                );
            }
        } else {
            return new ProviderFlightShopResponse(new FlightShopResponse());
        }

    }

    @Override
    public String getName() {
        return "sabre-outbound";
    }

    @Override
    public String getDescription() {
        return "Sabre REST outbound flights";
    }

    @Override
    public int clearCaches() {
        return 0;
    }

}
