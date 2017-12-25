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

import com.allegianttravel.ota.flight.shop.rest.dto.Severity;

import javax.annotation.Generated;
import java.util.Optional;

/**
 * Models an exception encountered when invoking a supporting service. This could be before the call because the
 * endpoint wasn't configured, during the call with an IOException, or after the call when dealing with the response.
 */
public abstract class SupportingServiceException extends Exception {
    /**
     * endpoint for the supporting service
     */
    private final String endpoint;
    /**
     * HTTP status code from the response (may be null)
     */
    private final Integer code;

    /**
     * relevant portion of the payload for debugging. (may be null)
     */
    private final String payload;

    private final Severity severity;

    public SupportingServiceException(Severity severity, String message, String endpoint, Exception causedBy) {
        super(message, causedBy);
        this.severity = severity;
        this.endpoint = endpoint;
        this.code = null;
        this.payload = null;
    }

    public SupportingServiceException(Severity severity, String message, String endpoint, int code, String payload,
                                      Exception causedBy) {
        super(message, causedBy);
        this.severity = severity;
        this.endpoint = endpoint;
        this.code = code;
        this.payload = payload;
    }

    public Optional<Integer> getCode() {
        return Optional.ofNullable(code);
    }

    public Optional<String> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Generated("by IDE")
    public String getEndpoint() {
        return endpoint;
    }

    @Generated("by IDE")
    public Severity getSeverity() {
        return severity;
    }
}
