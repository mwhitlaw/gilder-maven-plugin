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
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FlightShopUtilsTest {
    @Test
    public void groupByFlightRequest() throws Exception {
        String path = "/HNL-LAS-20-options.json";

        AirportCode departAirportCode = new AirportCode("HNL");
        AirportCode arriveAirportCode = new AirportCode("LAS");
        LocalDate departDate = LocalDate.parse("2017-04-25");
        LocalDate returnDate = LocalDate.parse("2017-04-29");

        FlightShopRequest shopRequest = FlightShopRequest.builder()
                .setDepartAirportCode(departAirportCode)
                .setArriveAirportCode(arriveAirportCode)
                .setDepartDate(departDate)
                .setReturnDate(returnDate)
                .build();

        Map<FlightRequest, FlightOptions> optionsByRequest = FilterFixture.groupByFlightRequest(shopRequest, path);

        assertEquals(2, optionsByRequest.size());
        assertEquals(20, optionsByRequest.get(new FlightRequest(departAirportCode, arriveAirportCode, departDate)).getFlightOptions().size());
        assertEquals(20, optionsByRequest.get(new FlightRequest(arriveAirportCode, departAirportCode, returnDate)).getFlightOptions().size());
    }
}
