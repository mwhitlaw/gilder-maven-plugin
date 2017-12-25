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

import com.allegiant.util.gen2.JsonUtils;
import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class G4ShopDtoToFlightOptionMapperTest {

    private final String input;
    private final String expected;
    private final FlightShopRequest request;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() throws Exception {
        return Arrays.asList(
                new Object[] {
                        "oneway",
                        FlightShopRequest.builder()
                                .setDepartAirportCode(new AirportCode("BLI"))
                                .setArriveAirportCode(new AirportCode("LAS"))
                                .setPassengers(PassengerTypeCount.singleton(PassengerType.ADULT,1))
                                .setDepartDate(LocalDate.parse("2017-01-27"))
                                .build(),
                        "/test1-g4shop-response.json",
                        "/expected-oneway.json"
                },
                new Object[] {
                        "oneway, 2 pax",
                        FlightShopRequest.builder()
                                .setDepartAirportCode(new AirportCode("BLI"))
                                .setArriveAirportCode(new AirportCode("LAS"))
                                .setDepartDate(LocalDate.parse("2017-01-27"))
                                .setPassengers(PassengerTypeCount.singleton(PassengerType.ADULT, 2))
                                .build(),
                        "/test1-g4shop-response.json",
                        "/expected-oneway-2pax.json"
                },
                new Object[] {
                        "roundtrip-coupon",
                        FlightShopRequest.builder()
                                .setDepartAirportCode(new AirportCode("BLI"))
                                .setArriveAirportCode(new AirportCode("LAS"))
                                .setPassengers(PassengerTypeCount.singleton(PassengerType.ADULT, 1))
                                .setDepartDate(LocalDate.parse("2017-08-22"))
                                .build(),
                        "/test2-g4shop-response.json",
                        "/test2-expected.json"
                }

                );
    }

    public G4ShopDtoToFlightOptionMapperTest(@SuppressWarnings("unused") String name,
                                             FlightShopRequest request,
                                             String input, String expected) {
        this.request = request;
        this.input =input;
        this.expected = expected;
    }

    @Test
    public void test() throws Exception {
        ShopResponseDto g4Response = JsonUtils.deserialize(asString(input), ShopResponseDto.class);

        G4ShopDtoToFlightOptionMapper mapper = new G4ShopDtoToFlightOptionMapper(
                Collections.singletonList(new G4ShopResponse(g4Response, request.getDepartDate())),
                request.getPassengers()
        );

        Map<FlightRequest, FlightOptions> optionsByRequest = mapper.groupByPairAndTravelDate();
        assertNotNull(optionsByRequest);

        assertEquals(1, optionsByRequest.size());
        assertTrue(optionsByRequest.containsKey(
                new FlightRequest(
                        new AirportCode("BLI"),
                        new AirportCode("LAS"),
                        request.getDepartDate()))
        );

        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        String actual = om.writeValueAsString(optionsByRequest.values().iterator().next());
        System.out.println(actual);

        String expected = asString(this.expected);
        assertJsonEquals(expected, actual);
    }
}
