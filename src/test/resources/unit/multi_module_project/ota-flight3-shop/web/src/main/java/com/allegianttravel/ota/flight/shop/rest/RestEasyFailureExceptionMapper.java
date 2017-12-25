/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jboss.resteasy.spi.Failure;

import javax.annotation.Generated;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Produces(MediaType.APPLICATION_JSON)
@Provider
public class RestEasyFailureExceptionMapper implements ExceptionMapper<Failure> {
    @Override
    public Response toResponse(final Failure exception) {
        return Response.status(exception.getErrorCode())
                .entity(new FailureDetails()
                        .setCode(exception.getErrorCode() + "")
                        .setMessage(exception.getMessage())
                        .setCausedByMessage(exception.getCause() != null ? exception.getCause().getMessage() : null))
                .build();
    }

    public static class FailureDetails {
        private String code;
        private String message;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String causedByMessage;

        @Generated("by IDE")
        public String getCode() {
            return code;
        }

        @Generated("by IDE")
        public FailureDetails setCode(final String code) {
            this.code = code;
            return this;
        }

        @Generated("by IDE")
        public String getMessage() {
            return message;
        }

        @Generated("by IDE")
        public FailureDetails setMessage(final String message) {
            this.message = message;
            return this;
        }

        @Generated("by IDE")
        public String getCausedByMessage() {
            return causedByMessage;
        }

        @Generated("by IDE")
        public FailureDetails setCausedByMessage(final String causedByMessage) {
            this.causedByMessage = causedByMessage;
            return this;
        }
    }
}
