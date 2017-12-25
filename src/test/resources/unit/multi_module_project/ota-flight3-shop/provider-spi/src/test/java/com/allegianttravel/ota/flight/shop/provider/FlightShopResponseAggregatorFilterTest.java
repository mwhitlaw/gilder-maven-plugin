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
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FlightShopResponseAggregatorFilterTest {
    @Test
    public void mergeFromSameProvider() throws Exception {
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

        Exchange exchange = new Exchange();
        exchange.addOut(new ProviderFlightShopResponse(new FlightShopResponse().setFlightOptions(optionsByRequest)));

        FlightShopResponseAggregatorFilter filter = new FlightShopResponseAggregatorFilter();
        filter.process(exchange);

        assertEquals(1, exchange.getOuts().size());

        ProviderFlightShopResponse output = (ProviderFlightShopResponse) exchange.getOuts().get(0);
        FlightShopResponse response = output.getResponse();
        Map<FlightRequest, FlightOptions> unifiedGroupedByFlightRequest = response.getFlightOptions();
        assertEquals(2, unifiedGroupedByFlightRequest.size());


    }
}
