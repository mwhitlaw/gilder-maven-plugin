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
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.MultiCityShopRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

public class G4MultiFlightShopProviderIT {
    @Test
    public void test() throws Exception {
        MultiCityShopRequest request = new MultiCityShopRequest()
            .setBookingType("WB")
            .setChannelId(1)
            .setPassengers(Collections.singletonList(new PassengerTypeCount(PassengerType.ADULT, 1)));
        /*
            BLI - LAS - LAX - BLI
         */
        LocalDate travelDate = LocalDate.parse("2017-03-01");
        request.setFlights(Arrays.asList(
                new FlightRequest(new AirportCode("BLI"), new AirportCode("LAS"), travelDate),
                new FlightRequest(new AirportCode("LAS"), new AirportCode("LAX"), travelDate.plusDays(3)),
                new FlightRequest(new AirportCode("LAX"), new AirportCode("BLI"), travelDate.plusDays(5))
        ));

        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        String json = om.writeValueAsString(request);
        System.out.println(json);
    }
}
