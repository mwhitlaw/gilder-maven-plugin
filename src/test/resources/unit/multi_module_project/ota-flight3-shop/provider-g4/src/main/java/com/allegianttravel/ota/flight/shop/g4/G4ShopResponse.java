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

import com.allegiantair.g4flights.service.client.dto.ShopDto;
import com.allegiantair.g4flights.service.client.response.ShopFlightResponseDto;
import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;

import javax.annotation.Generated;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.allegianttravel.ota.flight.shop.g4.G4ErrorMessageUtil.extractErrorMessages;


public class G4ShopResponse {
    private final LocalDate travelDate;
    private final List<ErrorMessage> errorMessages;
    private final List<ShopDto> shopDtos;

    G4ShopResponse(ShopResponseDto response, LocalDate travelDate) {
        this.travelDate = travelDate;
        this.errorMessages = extractErrorMessages(response);
        this.shopDtos = response.getShopDtos() != null ? response.getShopDtos() : Collections.emptyList();
    }

    G4ShopResponse(ShopFlightResponseDto response, LocalDate travelDate) {
        this.travelDate = travelDate;
        this.errorMessages = extractErrorMessages(response);
        this.shopDtos = response.getShopDto() != null ?
                Collections.singletonList(response.getShopDto()) : Collections.emptyList();
    }

    int size() {
        return shopDtos.size();
    }

    public List<ShopDto> getShopDtos() {
        return shopDtos;
    }

    boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    @Generated("by IDE")
    public LocalDate getTravelDate() {
        return travelDate;
    }


    @Generated("by IDE")
    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
