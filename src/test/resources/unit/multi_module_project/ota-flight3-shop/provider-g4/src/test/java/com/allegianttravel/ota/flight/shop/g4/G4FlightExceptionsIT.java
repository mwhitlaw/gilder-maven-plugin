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

import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class G4FlightExceptionsIT {
    private static LocalDate bookingDate = LocalDate.parse("2017-01-26");
    private static LocalDate travelDate = LocalDate.parse("2017-01-27");

    @Before
    public void setup() throws Exception {
        System.setProperty(G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.getKey(),
                // transposed the letters in the name, this should result in UnknownHostException
                "http://jbsch1:10780/g4-flights-shop/v1/api");
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.getKey());
    }

    @Test
    public void test() throws Exception {
        AbstractG4FlightShopProvider provider = new G4OutboundFlightShopProvider();
        FlightShopRequest request = FlightShopRequest.builder(createRequest()).build();

        ProviderFlightShopResponse output = (ProviderFlightShopResponse) provider.provide(request);

        assertTrue(output.getResponse().getErrorMessages().get(0).getMessage().startsWith("UnknownHostException"));

        System.out.println(output);
    }

    private static FlightShopRequest createRequest() {
        return FlightShopRequest
                    .builder()
                    .setBookingDate(bookingDate)
                    .setDepartDate(travelDate)
                    .setChannelId(1)
                    .setPassengers(Collections.singletonList(new PassengerTypeCount(PassengerType.ADULT, 1)))
                    .setDepartAirportCode(new AirportCode("BLI"))
                    .setArriveAirportCode(new AirportCode("LAS"))
                    .setBookingType("WB")
                    .build();
    }

}
