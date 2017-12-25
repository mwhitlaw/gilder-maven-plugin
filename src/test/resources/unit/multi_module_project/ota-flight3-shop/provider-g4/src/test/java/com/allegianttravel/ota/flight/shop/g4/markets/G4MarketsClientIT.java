/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.markets;

import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight3.MarketName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertTrue;

public class G4MarketsClientIT {
    private JettyServerFixture jettyServerFixture;

    @Before
    public void init() throws Exception {
        jettyServerFixture = new JettyServerFixture();
        jettyServerFixture.start();
        System.setProperty(G4FlightProperties.G4_OTA_MAINT_ENDPOINT.getKey(), jettyServerFixture.getUrl("/"));
    }

    @After
    public void tearDown() throws Exception {
        jettyServerFixture.assertSatisfied();
        jettyServerFixture.stop();
    }

    @AfterClass
    public static void destroy() {
        System.clearProperty(G4FlightProperties.G4_OTA_MAINT_ENDPOINT.getKey());
    }

    @Test
    public void singleMarket() throws Exception {

        jettyServerFixture.addServlet("/markets",
                RequestHandlerBuilder
                        .ok()
                        .payload(asString("/blilas-market.json"))
                        .expectedHeader("Accept", "application/json")
                        .contentType("application/json")
                        .build());

        // trigger a call
        MarketServiceImpl svc = new MarketServiceImpl();
        boolean allowed = svc.isAllowed(new MarketName("BLILAS"));
        assertTrue(allowed);
    }
}
