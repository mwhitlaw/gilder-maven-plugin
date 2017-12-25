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

import com.allegiant.jetty.HttpMethods;
import com.allegiant.jetty.JettyServerFixture;
import com.allegiant.jetty.RequestHandlerBuilder;
import com.allegiant.util.gen2.JsonUtils;
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
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQ;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQV1951Schema;
import com.allegianttravel.sabre.generated.OriginDestinationInformation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SabreFlightShopProviderIT {

    private JettyServerFixture jetty;

    @Before
    public void setup() throws Exception {
        jetty = new JettyServerFixture();
        jetty.start();
        SabreUtils.setAuthToken("foo");
        System.setProperty(SabreProperties.ENDPOINT.getKey(), jetty.getUrl("/sabre-service"));
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(SabreProperties.ENDPOINT.getKey());
        jetty.stop();
        jetty.assertSatisfied();
    }

    @Test
    public void test() throws Exception {

        jetty.addServlet("/sabre-service/*",
            RequestHandlerBuilder
                    .ok()
                    .expectedMethod(HttpMethods.POST)
                    .expectedHeader("Authorization", "Bearer " + SabreUtils.getAuthToken())
                    .requestAssertion(request -> {
                        try (Reader reader = request.getReader()) {
                            String json = IOUtils.toString(reader);
                            ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
                            OTAAirLowFareSearchRQV1951Schema schema = om.readValue(json, OTAAirLowFareSearchRQV1951Schema.class);
                            OTAAirLowFareSearchRQ searchRQ = schema.getOTAAirLowFareSearchRQ();
                            assertEquals(2, searchRQ.getOriginDestinationInformation().size());
                            OriginDestinationInformation outbound = searchRQ.getOriginDestinationInformation().get(0);
                            assertEquals("BOS", outbound.getOriginLocation().getLocationCode());
                            assertEquals("LAS", outbound.getDestinationLocation().getLocationCode());
                            OriginDestinationInformation inbound = searchRQ.getOriginDestinationInformation().get(1);
                            assertEquals("LAS", inbound.getOriginLocation().getLocationCode());
                            assertEquals("BOS", inbound.getDestinationLocation().getLocationCode());
                        } catch (IOException e) {
                            e.printStackTrace();
                            fail("error reading request payload");
                        }
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .payload(asString("/BOS-LAS-roundtrip-response.json"))
                    .build()
        );

        SabreRoundTripProvider provider = new SabreRoundTripProvider();

        LocalDate travelDate = LocalDate.now().plusDays(15);

        FlightShopRequest input = FlightShopRequest
                .builder()
                .setBookingDate(LocalDate.now())
                .setReturnDate(travelDate.plusDays(10))
                .setReqMinusDays(7)
                .setReqPlusDays(7)
                .setDepartDate(travelDate)
                .setChannelId(1)
                .setPassengers(Collections.singletonList(new PassengerTypeCount(PassengerType.ADULT, 1)))
                .setDepartAirportCode(new AirportCode("BOS"))
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
        ProviderOutput providerOutput = provider.provide(input);
        assertNotNull(providerOutput);

        FlightShopResponse flightOptions = ((ProviderFlightShopResponse)providerOutput).getResponse();

        List<FlightOption> flattened = flightOptions.getFlightOptions().values()
                .stream()
                .flatMap(fo->fo.getFlightOptions().stream())
                .collect(Collectors.toList());

        List<String> actual = flattened.stream().map(flightOption -> {
            Segment segment = flightOption.getSegments().get(0);
            Leg leg = segment.getLegs().get(0);
            return segment.getCarrierCode() + "-" + segment.getFlightNumber() + "-" + leg.getOrigin() + "-" + leg.getDestination() + "-" + leg.getDepartureTime();
        }).collect(Collectors.toList());

        // simple string version of the payload for the purposes of asserting the outcome
        assertEquals(Arrays.asList(
                "NK-641-BOS-LAS-2017-03-04T17:21",  // non-stop Spirit outbound
                "AA-252-BOS-ORD-2017-03-04T14:05",      // first leg of American outbound
                "AA-1404-BOS-ORD-2017-03-04T07:00",     // first leg of another American outbound
                "NK-640-LAS-BOS-2017-03-14T22:00",      // non-stop Spirit return
                "AA-431-LAS-CLT-2017-03-14T22:59",      // first leg of American return
                "AA-431-LAS-CLT-2017-03-14T22:59"), actual); // first leg of another American return
    }
}
