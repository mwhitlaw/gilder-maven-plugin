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
import com.allegianttravel.ota.flight.shop.rest.dto.travel.ConnectionInfo;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Filter;

import javax.inject.Named;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Ensures that each FlightOption has a ConnectionInfo on its relevant legs and sets the duration of the connection
 */
@Named
public class AddConnectionInfoFilter implements Filter {

    @Override
    @Profile
    public void process(Exchange exchange) {
        Optional<FlightShopResponse> optResponse = FilterUtil.getResponse(exchange);

        optResponse
                .ifPresent(flightShopResponse -> flightShopResponse.getFlightOptions().values()
                .stream()
                .flatMap(fos -> fos.getFlightOptions().stream())
                .forEach(this::decorate));

    }

    void decorate(FlightOption fo) {
        Leg previousLeg = null;
        for (Segment segment : fo.getSegments()) {
            for (Leg leg : segment.getLegs()) {
                if (previousLeg == null) {
                    previousLeg = leg;
                } else {
                    // the connection info is the amount of time the person sits in the airport
                    // waiting for the plane to take off
                    if (leg.getConnectionInfo() == null) {
                        leg.setConnectionInfo(new ConnectionInfo());
                    }
                    ConnectionInfo connectionInfo = leg.getConnectionInfo();
                    int between = (int) ChronoUnit.MINUTES.between(
                            previousLeg.getGmtArrivalTime().toInstant(ZoneOffset.UTC),
                            leg.getGmtDepartureTime().toInstant(ZoneOffset.UTC));
                    connectionInfo.setConnectionDuration(Math.abs(between));
                    previousLeg = leg;
                }
            }
        }
    }
}
