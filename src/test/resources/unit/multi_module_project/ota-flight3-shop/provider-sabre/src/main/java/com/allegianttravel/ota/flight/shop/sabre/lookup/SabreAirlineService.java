/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre.lookup;

import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface SabreAirlineService {
    @Headers({"Accept: application/json"})
    @GET("v1/lists/utilities/airlines")
    Call<SabreAirlineResponse> getAirline(@Query("airlinecode") AirlineCode airlineCode);
}
