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

import com.allegiantair.g4flights.service.client.dto.StatusDto;
import com.allegiantair.g4flights.service.client.response.BaseResponseDto;
import com.allegianttravel.ota.flight.shop.rest.dto.ErrorMessage;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class G4ErrorMessageUtil {

    private static final int ERROR_CODE = 500;

    private G4ErrorMessageUtil() {

    }

    /**
     * Extracts error messages from the G4 payload into our error message format
     *
     * @param response
     * @return
     */
    static List<ErrorMessage> extractErrorMessages(BaseResponseDto response) {
        if (response.getResponseStatusDto() != null && response.getResponseStatusDto().getErrors() != null) {
            return statusDtosToErrorMessages(response);
        } else {
            return Collections.emptyList();
        }
    }

    private static List<ErrorMessage> statusDtosToErrorMessages(BaseResponseDto response) {
        List<ErrorMessage> errorMessages1 = null;
        if (response.getResponseStatusDto().getErrors() != null) {
            errorMessages1 = new ArrayList<>();
            toErrorMessages(errorMessages1, Severity.ERROR, response.getResponseStatusDto().getErrors());
        }
        if (response.getResponseStatusDto().getWarnings() != null) {
            if (errorMessages1 == null) {
                errorMessages1 = new ArrayList<>();
            }
            toErrorMessages(errorMessages1, Severity.WARNING, response.getResponseStatusDto().getWarnings());
        }
        return errorMessages1;
    }

    private static void toErrorMessages(List<ErrorMessage> errorMessages, Severity severity,
                                        List<StatusDto> statusDtoList) {

        for (StatusDto statusDto : statusDtoList) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setProviderId("G4");
            errorMessage.setSeverity(severity);
            try {
                errorMessage.setCode(Integer.parseInt(statusDto.getCode()));
                errorMessage.setMessage(statusDto.getMessage());
            } catch (NumberFormatException e) {
                errorMessage.setCode(ERROR_CODE);
                errorMessage.setMessage(statusDto.getCode() + ":" + statusDto.getMessage());
            }
            errorMessages.add(errorMessage);
        }
    }
}
