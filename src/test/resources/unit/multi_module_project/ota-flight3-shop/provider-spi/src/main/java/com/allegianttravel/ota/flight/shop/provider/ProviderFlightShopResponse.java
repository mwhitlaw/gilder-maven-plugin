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

import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;

import javax.annotation.Generated;

public class ProviderFlightShopResponse extends ProviderOutput {
    private final FlightShopResponse response;

    public ProviderFlightShopResponse(FlightShopResponse response) {
        this.response = response;
    }

    @Generated("by IDE")
    public FlightShopResponse getResponse() {
        return response;
    }
}