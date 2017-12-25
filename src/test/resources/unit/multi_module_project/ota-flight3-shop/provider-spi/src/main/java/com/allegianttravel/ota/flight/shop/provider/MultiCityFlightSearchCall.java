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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;

import javax.annotation.Generated;

/**
 * Groups the FlightRequest and FlightOptions
 */
public class MultiCityFlightSearchCall {
    private final FlightRequest flightRequest;
    private final FlightOptions flightOptions;

    public MultiCityFlightSearchCall(FlightRequest flightRequest, FlightOptions flightOptions) {
        this.flightRequest = flightRequest;
        this.flightOptions = flightOptions;
    }

    @Generated("by IDE")
    public FlightRequest getFlightRequest() {
        return flightRequest;
    }

    @Generated("by IDE")
    public FlightOptions getFlightOptions() {
        return flightOptions;
    }
}
