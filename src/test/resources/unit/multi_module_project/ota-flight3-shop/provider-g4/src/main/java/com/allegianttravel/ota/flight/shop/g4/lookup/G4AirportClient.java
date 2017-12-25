/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.lookup;

import com.allegiantair.airport.dto.AirportDTO;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;

public interface G4AirportClient {
    @Headers({"Accept: application/json"})
    @GET("airport")
    Call<List<AirportDTO>> getAirports(@Query("iata") AirportCode departAirportCode);

}
