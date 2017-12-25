/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.faremaker;

import com.allegianttravel.ota.flight.shop.provider.SupportingServiceException;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;

public class FareMakerException extends SupportingServiceException {

    public FareMakerException(Severity severity, String message, String endpoint, Exception causedBy) {
        super(severity, message, endpoint, causedBy);
    }

    public FareMakerException(Severity severity, String message, String endpoint, int code, String payload,
                              Exception causedBy) {
        super(severity, message, endpoint, code, payload, causedBy);
    }
}
