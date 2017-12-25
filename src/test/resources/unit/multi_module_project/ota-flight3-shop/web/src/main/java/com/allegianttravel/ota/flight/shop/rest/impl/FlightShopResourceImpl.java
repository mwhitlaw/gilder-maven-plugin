/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.rest.impl;

import com.allegianttravel.ota.flight.shop.provider.CallerMetadata;
import com.allegianttravel.ota.flight.shop.provider.FlightNumberRequest;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderFlightShopResponse;
import com.allegianttravel.ota.flight.shop.rest.FareRuleType;
import com.allegianttravel.ota.flight.shop.rest.FlightShopResource;
import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCounts;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.framework.module.cdi.Exchange;
import com.allegianttravel.ota.framework.module.cdi.Service;
import com.allegianttravel.ota.framework.module.cdi.ServiceConfig;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class FlightShopResourceImpl implements FlightShopResource {

    private static final int NOT_FOUND = 404;

//    @Inject
//    @ServiceConfig("multicity.yml")
//    private Service multicityService;

    @Inject
    @ServiceConfig("flightshop.yml")
    private Service shopService;

    @Inject
    @ServiceConfig("flightshopByFlightNum.yml")
    private Service flightByNumberService;


    @Override
    @Profile
    @SuppressWarnings("checkstyle:ParameterNumber")
    public Response getPriceAvail(String departAirportCodeStr,
                                  String arriveAirportCodeStr,
                                  String departDateStr,
                                  String returnDateStr,
                                  int channelId,
                                  int passengers,
                                  String bookingType,
                                  int reqPlusDays,
                                  int reqMinusDays,
                                  String bookingDateStr,
                                  String csvCouponCodes,
                                  Integer requestSourceId,
                                  String namedFare,
                                  FareRuleType fareRuleType,
                                  String apiKey,
                                  String callerKey,
                                  String sessionKey,
                                  String callKey) {

        // the following assertions convey what we expect from the annotations on the interface
        assert departAirportCodeStr != null;
        assert arriveAirportCodeStr != null;
        assert departDateStr != null;
        assert bookingType != null;

        AirportCode departAirportCode = new AirportCode(departAirportCodeStr);
        AirportCode arriveAirportCode = new AirportCode(arriveAirportCodeStr);
        PassengerTypeCounts passengerTypeCounts = PassengerTypeCounts.singleType(PassengerType.ADULT, passengers);

        FareRuleType resolvedFareRuleType;
        if (fareRuleType == null) {
            resolvedFareRuleType = returnDateStr != null ? FareRuleType.ROUNDTRIP : FareRuleType.ONEWAY;
        } else {
            resolvedFareRuleType = fareRuleType;
        }

        List<String> couponCodes = toCouponCodes(csvCouponCodes);

        FlightShopRequest.Builder builder = FlightShopRequest.builder();
        builder.setDepartAirportCode(departAirportCode)
                .setArriveAirportCode(arriveAirportCode)
                .setCouponCodes(couponCodes)
                .setDepartDate(LocalDate.parse(departDateStr))
                .setChannelId(channelId)
                .setPassengers(passengerTypeCounts.getList())
                .setBookingType(bookingType)
                .setReqPlusDays(reqPlusDays)
                .setReqMinusDays(reqMinusDays)
                .setNamedFare(namedFare)
                .setFareRuleType(resolvedFareRuleType)
                .setRequestSourceId(requestSourceId)
                .setCallerMetadata(
                        CallerMetadata.builder()
                                .setCallerKey(callerKey)
                                .setApiKey(apiKey)
                                .setCallKey(callKey)
                                .setSessionKey(sessionKey).build()
                );
        if (returnDateStr != null && !returnDateStr.isEmpty()) {
            builder.setReturnDate(LocalDate.parse(returnDateStr));
        }
        if (bookingDateStr != null && !bookingDateStr.isEmpty()) {
            builder.setBookingDate(LocalDate.parse(bookingDateStr));
        }
        Exchange exchange = new Exchange();
        FlightShopRequest flightShopRequest = builder.build();
        exchange.setIn(flightShopRequest);
        shopService.process(exchange);

        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);
        return createResponse(outputs);
    }

    @Override
    @Profile
    @SuppressWarnings("checkstyle:ParameterNumber")
    public Response getFlightByFlightNum(String flightNum, String departDateStr, int channelId,
                                         int passengers, String fareClass, String bookingDateStr,
                                         String bookingType, String csvCouponCodes, String namedFare,
                                         FareRuleType fareRuleType,
                                         String apiKey, String callerKey,
                                         String sessionKey, String callKey) {

        FlightNumberRequest.Builder builder = FlightNumberRequest.builder();
        builder.setFlightNumber(new FlightNumber(flightNum))
                .setDepartDate(LocalDate.parse(departDateStr))
                .setChannelId(channelId)
                .setFareClass(fareClass)
                .setNamedFare(namedFare)
                .setPassengers(new PassengerTypeCounts(passengers + "A").getList())
                .setBookingType(bookingType)
                .setCouponCodes(toCouponCodes(csvCouponCodes))
                .setFareRuleType(fareRuleType == null ? FareRuleType.ONEWAY : fareRuleType)
                .setCallerMetadata(
                        CallerMetadata.builder()
                                .setCallerKey(callerKey)
                                .setApiKey(apiKey)
                                .setCallKey(callKey)
                                .setSessionKey(sessionKey).build()
                );
        if (bookingDateStr != null && !bookingDateStr.isEmpty()) {
            builder.setBookingDate(LocalDate.parse(bookingDateStr));
        }
        Exchange exchange = new Exchange();
        FlightNumberRequest flightShopRequest = builder.build();
        exchange.setIn(flightShopRequest);
        flightByNumberService.process(exchange);

        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);

        return createResponse(outputs);
    }

//    @Override
//    @Profile
//    public Response multicity(MultiCityShopRequest shopRequest, String apiKey, String callerKey,
//                      String sessionKey, String callKey) {
//
//        ProviderMultiCityShopRequest multiCityShopRequest = new ProviderMultiCityShopRequest(shopRequest,
//                CallerMetadata.builder()
//                .setCallerKey(callerKey)
//                .setApiKey(apiKey)
//                .setCallKey(callKey)
//                .setSessionKey(sessionKey).build());
//
//        Exchange exchange = new Exchange();
//        exchange.setIn(multiCityShopRequest);
//        multicityService.process(exchange);
//
//        List<ProviderFlightShopResponse> outputs = exchange.getOuts(ProviderFlightShopResponse.class);
//
//        return createResponse(outputs, shopRequest.getFlights());
//    }

    private String toSortKey(FlightRequest fr) {
        return fr.getTravelDate() + "" + fr.getOrigin() + "" + fr.getDestination();
    }

    private List<String> toCouponCodes(String rawCouponCodes) {
        List<String> couponCodes = rawCouponCodes.isEmpty() ? Collections.emptyList() :
                Arrays.asList(rawCouponCodes.split(","));
        return couponCodes
                .stream()
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .collect(Collectors.toList());
    }


    private Response createResponse(List<ProviderFlightShopResponse> outputs) {
        if (outputs.isEmpty()) {
            return Response.status(NOT_FOUND).build();
        } else {
            ProviderFlightShopResponse entity = outputs.get(0);
            FlightShopResponse flightShopResponse = entity.getResponse();
            Map<FlightRequest, FlightOptions> options = flightShopResponse.getFlightOptions();
            Map<FlightRequest, FlightOptions> sorted = new TreeMap<>(Comparator.comparing(this::toSortKey));
            sorted.putAll(options);
            flightShopResponse.setFlightOptions(sorted);

            // todo - not happy with the code below
            // it should be easier to know what type of http status code to return from provider errors.
            List<ErrorMessage> errorMessages = flightShopResponse.getErrorMessages();
            if (errorMessages != null && !errorMessages.isEmpty()) {
                return Response.status(errorMessages.stream()
                        .filter(em -> em.getSeverity() == Severity.ERROR)
                        .mapToInt(ErrorMessage::getCode)
                        .filter(code -> code >= Response.Status.BAD_REQUEST.getStatusCode() ||
                                code <= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .max()
                        .orElseGet(Response.Status.OK::getStatusCode))
                        .entity(flightShopResponse).build();
            } else {
                return Response.ok(flightShopResponse).build();
            }
        }
    }
}
