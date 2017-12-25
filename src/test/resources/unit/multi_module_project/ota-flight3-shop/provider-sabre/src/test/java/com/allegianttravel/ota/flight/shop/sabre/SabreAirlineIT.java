/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre;

import com.allegiant.jetty.HttpMethods;
import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airline;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.sabre.lookup.SabreAirlineLookup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertNotNull;

public class SabreAirlineIT {
    private JettyServerFixture jetty;

    @Before
    public void setup() throws Exception {
        jetty = new JettyServerFixture();
        jetty.start();
        SabreUtils.setAuthToken("foo");
        System.setProperty(SabreProperties.ENDPOINT.getKey(), jetty.getUrl("/"));
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(SabreProperties.ENDPOINT.getKey());
        jetty.stop();
    }

    @Test
    public void test() throws Exception {

        jetty.addServlet("/v1/lists/utilities/airlines",
                RequestHandlerBuilder
                        .ok()
                        .expectedMethod(HttpMethods.GET)
                        .expectedHeader("Authorization", "Bearer " + SabreUtils.getAuthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .payload(asString("/sabre-airline.json"))
                        .build()
        );

        SabreAirlineLookup.AirlineCacheLoader lookup = new SabreAirlineLookup.AirlineCacheLoader();

        Airline airline = lookup.load(AirlineCode.of("AA"));

        jetty.assertSatisfied();
        assertNotNull(airline);
    }
}
