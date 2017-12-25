/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.provider;

import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;

import java.util.Optional;

public final class SupportingServiceExceptionUtil {

    private SupportingServiceExceptionUtil() {

    }

    public static void handleSupportingServiceException(String providerId, final SupportingServiceException cause,
                                                        final FlightShopResponse flightShopResponse) {
        ErrorMessage errorMessage = new ErrorMessage().setSeverity(cause.getSeverity());
        errorMessage.setProviderId(providerId);
        String message = String.format("%s endpoint: %s payload: %s", cause.getMessage(), cause.getEndpoint(),
                cause.getPayload().orElse("none"));
        errorMessage.setMessage(message);
        Optional<Integer> code = cause.getCode();
        code.ifPresent(errorMessage::setCode);
        flightShopResponse.addErrorMessage(errorMessage);
    }
}
