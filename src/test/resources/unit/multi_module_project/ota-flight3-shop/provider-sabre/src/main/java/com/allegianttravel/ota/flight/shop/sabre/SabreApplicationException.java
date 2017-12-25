/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.sabre;

import javax.annotation.Generated;
import java.util.Optional;

public class SabreApplicationException extends RuntimeException {
    private final String payload;
    private final int status;

    public SabreApplicationException(String message, String payload, int status) {
        super(message);
        this.payload = payload;
        this.status = status;
    }

    public SabreApplicationException(String message, String payload, int status, Throwable causedBy) {
        super(message, causedBy);
        this.payload = payload;
        this.status = status;
    }

    public Optional<String> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Generated("by IDE")
    public int getStatus() {
        return status;
    }
}
