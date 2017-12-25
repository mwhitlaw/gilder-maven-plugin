/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4;

import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegianttravel.ota.flight.shop.g4.ancillary.G4AncillaryUtil;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertNotNull;

public class G4AncillaryUtilIT {
    private JettyServerFixture jettyServerFixture;

    @Before
    public void init() throws Exception {
        jettyServerFixture = new JettyServerFixture();
        jettyServerFixture.start();
        System.setProperty(G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENDPOINT.getKey(),
                jettyServerFixture.getUrl("/"));
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENDPOINT.getKey());
    }

    @Test
    public void test() throws Exception {

        jettyServerFixture.addServlet("/ancillaryfees",
                RequestHandlerBuilder
                        .ok()
                        .payload(asString("/multidate-fees-response.json"))
                        .expectedParam("departAirportCode", "BLI")
                        .expectedParam("arriveAirportCode", "LAS")
                        .expectedParam("bookingDate", "2017-09-03")
                        .expectedParam("departDate", "2017-10-10")
                        .expectedParam("reqPlusDays", "0")
                        .expectedParam("reqMinusDays", "0")
                        .expectedParam("channelId", "1")
                        .noExtraParams()
                        .contentType("application/json")
                        .build());

//        // trigger a call
        FlightFees flightFees = G4AncillaryUtil.getFlightFeesForDaysOfTravel(
                FlightShopRequest.builder()
                        .setDepartAirportCode(AirportCode.of("BLI"))
                        .setArriveAirportCode(AirportCode.of("LAS"))
                        .setDepartDate(LocalDate.parse("2017-10-10"))
                        .setBookingDate(LocalDate.parse("2017-09-03"))
                        .setChannelId(1)
                        .build());

        jettyServerFixture.assertSatisfied();
        jettyServerFixture.stop();

        assertNotNull(flightFees);
    }

}
