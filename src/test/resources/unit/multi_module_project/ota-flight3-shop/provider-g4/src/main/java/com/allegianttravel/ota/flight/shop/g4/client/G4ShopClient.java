/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.client;

import com.allegiantair.g4flights.service.client.response.ShopFlightResponseDto;
import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.time.LocalDate;

public interface G4ShopClient {

    @Headers({"Accept: application/json"})
    @GET("flights/shop")
    @SuppressWarnings("checkstyle:ParameterNumber")
    Call<ShopResponseDto> shopFlights(
            @Query("departAirportCode") AirportCode departAirportCode,
            @Query("arriveAirportCode") AirportCode arriveAirportCode,
            @Query("searchStartDate") LocalDate searchStartDate,
            @Query("searchEndDate") LocalDate searchEndDate,
            @Query("channelId") int channelId,
            @Query("passengerCount") int passengerCount,
            @Query("bookingDate") LocalDate bookingDate,
            @Query("bookingType") String bookingType,
            @Query("couponCodes") String couponCodes,
            @Query("isRoundTrip") boolean isRoundTrip,
            @Query("requestSourceId") Integer requestSourceId
            );



    @Headers({"Accept: application/json"})
    @GET("flights/shop-flight")
    Call<ShopFlightResponseDto> shopByFlightNumber(
            @Query("flightNum") FlightNumber flightNumber,
            @Query("departDate") LocalDate departDate,
            @Query("channelId") int channelId,
            @Query("isRoundTrip") boolean isRoundTrip,
            @Query("passengerCount") int passengerCount,
            @Query("bookingDate") LocalDate bookingDate,
            @Query("bookingType") String bookingType,
            @Query("couponCodes") String couponCodes,
            @Query("fareClasses") String fareClass
    );

}
