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

import com.allegiant.jetty.HttpMethods;
import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandler;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegiantair.g4flights.domain.Channel;
import com.allegianttravel.ota.flight.shop.provider.CallerMetadata;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class G4FlightShopProviderIT {
    private JettyServerFixture jetty;
    private static LocalDate bookingDate = LocalDate.parse("2017-01-26");
    private static LocalDate travelDate = LocalDate.parse("2017-01-27");
    private List<AbstractG4FlightShopProvider> providers = Arrays.asList(new G4OutboundFlightShopProvider(), new G4InboundFlightShopProvider());

    private final FlightShopRequest request;
    private final List<RequestHandler> requestHandlers;
    // simple string version of the payload for the purposes of asserting the outcome
    private final List<String> summarizedOptions;

    public G4FlightShopProviderIT(FlightShopRequest request, List<RequestHandler> requestHandlers, List<String> summarizedOptions) {
        this.request = request;
        this.requestHandlers = requestHandlers;
        this.summarizedOptions = summarizedOptions;
    }

    @Parameterized.Parameters
    public static List<Object[]> params() throws Exception {

        List<Object[]> params = new ArrayList<>();

        {
            FlightShopRequest input = createRequest();
            List<RequestHandler> onewayHandler = Collections.singletonList(RequestHandlerBuilder
                    .ok()
                    .expectedMethod(HttpMethods.GET)
                    .expectedParam("departAirportCode", input.getDepartAirportCode().toString())
                    .expectedParam("arriveAirportCode", input.getArriveAirportCode().toString())
                    .expectedParam("bookingDate", input.getBookingDate().toString())
                    .expectedParam("channelId", Channel.WEB.getId()+"")
                    .expectedParam("passengerCount", "1")
                    .expectedParam("bookingType", com.allegiantair.g4flights.domain.BookingType.WEB_BOOKING.getBookingTypeCode())
                    .expectedParam("searchStartDate", input.getDepartDate().toString())
                    .expectedParam("searchEndDate", input.getDepartDate().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .payload(asString("/IT-BLI-LAS-response.json"))
                    .build());

            // oneway flight no longer has a markup
            params.add(new Object[]{input, onewayHandler, Collections.singletonList("G4-1810-BLI-LAS-2017-02-21T08:00-[STD=USD104.00]")});
        }

        {
            FlightShopRequest input = FlightShopRequest.builder(createRequest())
                    .setReturnDate(travelDate.plusDays(7))
                    .build();
            List<RequestHandler> roundTrip = Arrays.asList(
                    /* OUTBOUND */
                    RequestHandlerBuilder
                            .ok()
                            .expectedMethod(HttpMethods.GET)
                            .expectedParam("departAirportCode", input.getDepartAirportCode().toString())
                            .expectedParam("arriveAirportCode", input.getArriveAirportCode().toString())
                            .expectedParam("bookingDate", input.getBookingDate().toString())
                            .expectedParam("channelId", Channel.WEB.getId()+"")
                            .expectedParam("passengerCount", "1")
                            .expectedParam("bookingType", com.allegiantair.g4flights.domain.BookingType.WEB_BOOKING.getBookingTypeCode())
                            .expectedParam("searchStartDate", input.getDepartDate().toString())
                            .expectedParam("searchEndDate", input.getDepartDate().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .payload(asString("/IT-BLI-LAS-response.json"))
                            .build(),
                    /* INBOUND */
                    RequestHandlerBuilder
                            .ok()
                            .expectedMethod(HttpMethods.GET)
                            .expectedParam("departAirportCode", input.getArriveAirportCode().toString())
                            .expectedParam("arriveAirportCode", input.getDepartAirportCode().toString())
                            .expectedParam("bookingDate", input.getBookingDate().toString())
                            .expectedParam("channelId", Channel.WEB.getId()+"")
                            .expectedParam("passengerCount", "1")
                            .expectedParam("bookingType", com.allegiantair.g4flights.domain.BookingType.WEB_BOOKING.getBookingTypeCode())
                            .expectedParam("searchStartDate", input.getReturnDate().toString())
                            .expectedParam("searchEndDate", input.getReturnDate().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .payload(asString("/IT-LAS-BLI-response.json"))
                            .build()
            );

//            params.add(new Object[]{input, roundTrip, Arrays.asList(
//                    "G4-1810-BLI-LAS-2017-02-21T08:00-[RACK=USD104.00,FLEX=USD120.00,FLEX_BUNDLE=USD140.00]",
//                    "G4-1811-LAS-BLI-2017-02-28T15:18-[RACK=USD71.00,FLEX=USD91.00,FLEX_BUNDLE=USD110.00]")});
            params.add(new Object[]{input, roundTrip, Arrays.asList(
                    "G4-1810-BLI-LAS-2017-02-21T08:00-[STD=USD104.00]",
                    "G4-1811-LAS-BLI-2017-02-28T15:18-[STD=USD71.00]")});
        }
        return params;
    }


    @Before
    public void setup() throws Exception {
        jetty = new JettyServerFixture();
        jetty.start();
        System.setProperty(G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.getKey(), jetty.getUrl("/g4-service"));
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.getKey());
        jetty.stop();
        jetty.assertSatisfied();
    }

    @Test
    public void test() throws Exception {

        jetty.addServlet("/g4-service/flights/shop", requestHandlers);

        List<String> actuals = new ArrayList<>();

        for(AbstractG4FlightShopProvider provider : providers) {
            ProviderOutput providerOutput = provider.provide(request);
            assertNotNull(providerOutput);

            FlightShopResponse response = ((ProviderFlightShopResponse) providerOutput).getResponse();

            List<String> actual = toFlightDetails(response);

            actuals.addAll(actual);
        }

        assertEquals(summarizedOptions, actuals);
    }

    private List<String> toFlightDetails(FlightShopResponse response) {

        List<FlightOption> options = new ArrayList<>();
        response.getFlightOptions().values().forEach(flightOptions -> options.addAll(flightOptions.getFlightOptions()));

        return options.stream().map(flightOption -> {
                Segment segment = flightOption.getSegments().get(0);
                Leg leg = segment.getLegs().get(0);
                return String.join("-",
                        Stream.of(segment.getCarrierCode(),
                                    segment.getFlightNumber(),
                                    leg.getOrigin(),
                                    leg.getDestination(),
                                    leg.getDepartureTime(),
                                    "[" +
                                        String.join(",",
                                        flightOption.getProviderFares().get(0)
                                                .getNamedFares()
                                                .stream()
                                                .map(namedFare -> namedFare.getName() + "=" + namedFare.getFareSalesTotal())
                                                .collect(Collectors.toList()))
                                    + "]"
                        )
                        .map(Object::toString)
                        .collect(Collectors.toList()));
            }).collect(Collectors.toList());
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
                    .setMaxResults(10)
                    .setCallerMetadata(CallerMetadata
                            .builder()
                            .setApiKey("apiKey")
                            .setCallerKey("callerKey")
                            .setCallKey("callKey")
                            .setSessionKey("sessionKey")
                            .build())
                    .build();
    }

}
