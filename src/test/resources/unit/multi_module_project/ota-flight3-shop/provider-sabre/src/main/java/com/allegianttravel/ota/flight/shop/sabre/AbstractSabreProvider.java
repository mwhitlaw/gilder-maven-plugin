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
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQ;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRS;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.allegianttravel.ota.flight.shop.sabre.SabreRetrofitClient.search;
import static com.allegianttravel.ota.flight.shop.sabre.SabreUtils.createRoundtripRequest;

abstract class AbstractSabreProvider implements Provider {

    FlightOptions searchRoundTrip(final FlightShopRequest shopRequest, final Supplier<PackageId> packageSupplier)
            throws IOException {
        OTAAirLowFareSearchRQ roundtripRequest = createRoundtripRequest(shopRequest);
        // Use the SabreUtils helper class to invoke the service and then get the options back.




        Optional<OTAAirLowFareSearchRS> response = search(roundtripRequest);
        FlightOptions flightOptions;
        if (response.isPresent()) {
            flightOptions = new SabreToFlightOptionMapper(response.get(), packageSupplier).map();
        } else {
            flightOptions = new FlightOptions();
        }
        return flightOptions;
    }

}
