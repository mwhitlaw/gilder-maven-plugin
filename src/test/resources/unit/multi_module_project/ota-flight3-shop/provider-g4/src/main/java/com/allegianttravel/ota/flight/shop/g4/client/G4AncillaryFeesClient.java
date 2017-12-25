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

import com.allegiantair.g4flights.ancillary.service.rest.AncillaryFeesByDateResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.time.LocalDate;

public interface G4AncillaryFeesClient {

    @Headers({"Accept: application/json"})
    @GET("ancillaryfees")
    Call<AncillaryFeesByDateResponse> getFees(
            @Query("departAirportCode") AirportCode departAirportCode,
            @Query("arriveAirportCode") AirportCode arriveAirportCode,
            @Query("departDate") LocalDate departDate,
            @Query("channelId") int channelId,
            @Query("bookingDate") LocalDate bookingDate,
            @Query("reqPlusDays") int requestPlusDays,
            @Query("reqMinusDays") int requestMinusDays,
            @Query("returnDate") LocalDate returnDate
    );

}
