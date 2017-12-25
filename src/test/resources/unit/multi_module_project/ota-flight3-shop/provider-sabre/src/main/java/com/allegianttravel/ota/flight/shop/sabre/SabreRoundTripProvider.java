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

/**
 * Only responds to round trip search requests. There are separate Sabre providers to handle one-ways.
 */
@Named
public class SabreRoundTripProvider extends AbstractSabreProvider {

    private static final Logger logger = LoggerFactory.getLogger(SabreRoundTripProvider.class);

    @Override
    @Profile
    public ProviderOutput provide(ProviderInput input) {

        FlightShopRequest shopRequest = (FlightShopRequest) input;

        if (SabreUtils.isAllowed(shopRequest)) {

            try {
                logger.debug("servicing request for sabre {}", shopRequest);

                PackageSupplier packageSupplier = new PackageSupplier(getName());
                FlightOptions flightOptions = searchRoundTrip(shopRequest, packageSupplier);

                logger.debug("sabre responding with {} options", flightOptions.size());
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
        return "sabre-roundtrip";
    }

    @Override
    public String getDescription() {
        return "Sabre REST roundtrip flights";
    }

    @Override
    public int clearCaches() {
        return 0;
    }

}
