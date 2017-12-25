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

import com.allegiant.util.gen2.JsonUtils;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRSV1951Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class SabreToFlightShopMapperTest {

    private final String input;
    private final String expected;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() throws Exception {
        return Collections.singletonList(
                new Object[] {
                        "/BOS-LAS-roundtrip-response.json",
                        "/BOS-LAS-roundtrip-response-expected.json"
                }
                );
    }

    public SabreToFlightShopMapperTest(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void test() throws Exception {
        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        OTAAirLowFareSearchRSV1951Schema result = om.readValue(asString(input), OTAAirLowFareSearchRSV1951Schema.class);
        assertNotNull(result);

        SabreToFlightOptionMapper mapper = new SabreToFlightOptionMapper(result.getOTAAirLowFareSearchRS(), new Supplier<PackageId>() {
            AtomicInteger counter = new AtomicInteger(0);
            @Override
            public PackageId get() {
                return new PackageId("package-" + counter.getAndIncrement());
            }
        });
        FlightOptions actual = mapper.map();

        String json = om.writeValueAsString(actual);
        System.out.println(json);
        assertJsonEquals(asString(expected), json);

    }
}
