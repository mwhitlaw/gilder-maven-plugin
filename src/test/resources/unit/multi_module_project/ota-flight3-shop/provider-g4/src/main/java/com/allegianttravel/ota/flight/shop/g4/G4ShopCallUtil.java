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

import com.allegiant.util.gen2.DeserializationException;
import com.allegiant.util.gen2.JsonUtils;
import com.allegiantair.g4flights.service.client.dto.StatusDto;
import com.allegiantair.g4flights.service.client.response.BaseResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.Supplier;

public final class G4ShopCallUtil {

    private static final Logger logger = LoggerFactory.getLogger(G4ShopCallUtil.class);

    private G4ShopCallUtil() {

    }

    static <T extends BaseResponseDto> T execute(Call<T> call, Class<T> type, Supplier<T> bodyOnError)
            throws IOException {
        T shopResponseDto;
        Response<T> response = call.execute();
        if (response.isSuccessful()) {
            shopResponseDto = response.body();
        } else {
            String payload = "<missing>";
            try {
                payload = response.errorBody().string();
                logger.debug("Error invoking G4 client at {}. Status: {} Payload: {}",
                        call.request().url().toString(), response.code(), payload);
                shopResponseDto = JsonUtils.deserialize(payload, type);
            } catch (IOException | DeserializationException e) {
                // payload isn't a shop response dto, create a dummy one with an error
                shopResponseDto = bodyOnError.get();
                shopResponseDto.setError(new StatusDto(response.code() + "",
                        String.format("Error invoking G4 client at %s. Payload: %s", call.request().url().toString(),
                                payload)));
            }
        }
        return shopResponseDto;
    }

}
