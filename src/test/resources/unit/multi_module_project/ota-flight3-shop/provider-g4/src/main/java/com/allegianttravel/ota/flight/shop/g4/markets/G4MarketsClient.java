/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.markets;

import com.allegiant.ota.markets.model.MarketList;
import com.allegianttravel.ota.flight3.MarketName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface G4MarketsClient {
    @Headers({"Accept: application/json"})
    @GET("markets")
    Call<MarketList> getMarkets(@Query("marketName") MarketName marketName);

}
