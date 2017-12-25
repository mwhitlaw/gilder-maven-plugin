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
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.removeFakedOutRoundTripOutboundResults;

/**
 * Handles both round trip requests but only offers the return flight as an unpackaged option
 */
@Named
public class SabreInboundOnlyProvider extends AbstractSabreProvider {

    private static final Logger logger = LoggerFactory.getLogger(SabreInboundOnlyProvider.class);

    @Override
    @Profile
    public ProviderOutput provide(ProviderInput input) {
        FlightShopRequest shopRequest = (FlightShopRequest) input;

        if (shopRequest.getReturnDate() != null && SabreUtils.isAllowed(shopRequest)) {

            try {
                FlightShopRequest returnOnly = FlightShopRequest.builder(shopRequest)
                        .setDepartAirportCode(shopRequest.getArriveAirportCode())
                        .setArriveAirportCode(shopRequest.getDepartAirportCode())
                        .setDepartDate(shopRequest.getReturnDate())
                        .build();

                logger.debug("servicing request for sabre {}", returnOnly);

                //FlightOptions flightOptions = searchRoundTrip(new PackageSupplier(getName()), returnOnly);
                PackageSupplier packageSupplier = new PackageSupplier(getName());
                FlightOptions flightOptions = searchRoundTrip(returnOnly, packageSupplier);

                // remove all of the outbound flights
                // set the package to NONE on each of the return flights
                // cut the prices in half for the demo to approximate one-way
                removeFakedOutRoundTripOutboundResults(flightOptions, shopRequest.getDepartAirportCode());

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
        return "sabre-inbound";
    }

    @Override
    public String getDescription() {
        return "Sabre REST inbound flights";
    }

    @Override
    public int clearCaches() {
        return 0;
    }

}
