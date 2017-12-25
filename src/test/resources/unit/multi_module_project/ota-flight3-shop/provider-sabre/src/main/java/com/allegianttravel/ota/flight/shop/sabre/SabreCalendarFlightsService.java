/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre;

import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQV1951Schema;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRSV1951Schema;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SabreCalendarFlightsService {
    @Headers({"Accept: application/json"})
    @POST("vv1.9.5.1/shop/calendar/flights")
    Call<OTAAirLowFareSearchRSV1951Schema> search(
            @Body
            OTAAirLowFareSearchRQV1951Schema searchDetails
    );
}
