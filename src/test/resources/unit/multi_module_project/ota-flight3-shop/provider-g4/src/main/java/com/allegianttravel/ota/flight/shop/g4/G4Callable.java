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

import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import retrofit2.Call;

import javax.annotation.Generated;
import java.time.LocalDate;
import java.util.concurrent.Callable;

import static com.allegianttravel.ota.flight.shop.g4.G4ShopCallUtil.execute;

public class G4Callable implements Callable<G4ShopResponse> {
    private final Callable<Call<ShopResponseDto>> delegate;
    private final LocalDate travelDate;

    G4Callable(Callable<Call<ShopResponseDto>> delegate, LocalDate travelDate) {
        this.delegate = delegate;
        this.travelDate = travelDate;
    }

    @Override
    public G4ShopResponse call() throws Exception {
        Call<ShopResponseDto> call = delegate.call();
        ShopResponseDto shopResponseDto = execute(call, ShopResponseDto.class, ShopResponseDto::new);
        return new G4ShopResponse(shopResponseDto, travelDate);
    }

    @Generated("by IDE")
    public LocalDate getTravelDate() {
        return travelDate;
    }
}
