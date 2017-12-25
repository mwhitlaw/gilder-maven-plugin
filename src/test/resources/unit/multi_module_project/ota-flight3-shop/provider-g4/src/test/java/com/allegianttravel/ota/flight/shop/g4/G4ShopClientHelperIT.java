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
import com.allegiant.util.gen2.JsonUtils;
import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class G4ShopClientHelperIT {

    private final JettyServerFixture jetty = new JettyServerFixture();
    private final FlightShopRequest shopRequest = FlightShopRequest
            .builder()
            .setDepartDate(LocalDate.parse("2017-09-08"))
            .setBookingDate(LocalDate.parse("2017-09-08"))
            .setBookingType("WB")
            .build();

    @Before
    public void setUp() throws Exception {
        jetty.start();
    }

    @After
    public void tearDown() throws Exception {
        jetty.assertSatisfied();
        jetty.stop();
    }

    @Test
    public void clientResponseFailure_withPayload() throws Exception {

        String payload = asString("/g4client/shopResponse-errors.json");

        jetty.addServlet("/flights/shop",
                RequestHandlerBuilder
                        .status(500)
                        .contentType("application/json")
                        .payload(payload)
                        .build());


        // The first run of this test takes ~5 seconds due to the loading/creating of the RestEasy proxy.
        // It's much faster the next time around
        ShopResponseDto responseDto = G4ShopClientHelper.invoke(shopRequest, jetty.getUrl("/"));

        String actual = JsonUtils.serialize(responseDto, true);

        assertJsonEquals(payload, actual);
    }

    @Test
    public void clientResponseFailure_noPayload() throws Exception {

        jetty.addServlet("/flights/shop",
                RequestHandlerBuilder
                        .status(500)
                        .build());


        // The first run of this test takes ~5 seconds due to the loading/creating of the RestEasy proxy.
        // It's much faster the next time around
        ShopResponseDto responseDto = G4ShopClientHelper.invoke(shopRequest, jetty.getUrl("/"));

        String actual = JsonUtils.serialize(responseDto, true);

        System.out.println(actual);

        String expected = asString("/g4client/shopResponse-noPayload.json");
        StrSubstitutor subby = new StrSubstitutor(Collections.singletonMap("jetty-url", jetty.getUrl("/flights/shop")));
        expected = subby.replace(expected);
        assertJsonEquals(expected, actual);
    }

    @Test
    public void clientResponseFailure_notJson() throws Exception {

        jetty.addServlet("/flights/shop",
                RequestHandlerBuilder
                        .status(500)
                        .payload("this is not json")
                        .contentType("application/json")
                        .build());


        // The first run of this test takes ~5 seconds due to the loading/creating of the RestEasy proxy.
        // It's much faster the next time around
        ShopResponseDto responseDto = G4ShopClientHelper.invoke(shopRequest, jetty.getUrl("/"));

        String actual = JsonUtils.serialize(responseDto, true);

        System.out.println(actual);

        String expected = asString("/g4client/shopResponse-notJson.json");
        StrSubstitutor subby = new StrSubstitutor(Collections.singletonMap("jetty-url", jetty.getUrl("/flights/shop")));
        expected = subby.replace(expected);
        assertJsonEquals(expected, actual);
    }
}
