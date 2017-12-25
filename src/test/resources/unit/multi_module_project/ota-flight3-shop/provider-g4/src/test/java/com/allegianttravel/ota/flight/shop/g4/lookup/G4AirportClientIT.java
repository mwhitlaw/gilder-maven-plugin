/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.lookup;

import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.allegiant.tests.LoadFromClasspath.asString;

public class G4AirportClientIT {
    private JettyServerFixture jettyServerFixture;

    @Before
    public void init() throws Exception {
        jettyServerFixture = new JettyServerFixture();
        jettyServerFixture.start();
        System.setProperty(G4FlightProperties.G4_AIRPORTS.getKey(), jettyServerFixture.getUrl("/"));
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(G4FlightProperties.G4_AIRPORTS.getKey());
    }

    @Test
    public void test() throws Exception {

        jettyServerFixture.addServlet("/airport",
                RequestHandlerBuilder
                        .ok()
                        .payload(asString("/airport.json"))
                        .expectedParam("iata", "BLI")
                        .expectedHeader("Accept", "application/json")
                        .contentType("application/json")
                        .build());

        // trigger a call
        G4AirportLookup lookup = new G4AirportLookup();
        lookup.lookup(new AirportCode("BLI"));

        jettyServerFixture.assertSatisfied();
        jettyServerFixture.stop();
    }
}
