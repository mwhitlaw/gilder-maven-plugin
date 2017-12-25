/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre.lookup;

import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

public class SabreAirlineResponse {
    @JsonProperty("AirlineInfo")
    private List<AirlineEntry> airlineInfo;

    @Generated("by IDE")
    public List<AirlineEntry> getAirlineInfo() {
        return airlineInfo;
    }

    @Generated("by IDE")
    public SabreAirlineResponse setAirlineInfo(final List<AirlineEntry> airlineInfo) {
        this.airlineInfo = airlineInfo;
        return this;
    }

    public static class AirlineEntry {
        @JsonProperty("AirlineCode")
        private AirlineCode airlineCode;
        @JsonProperty("AirlineName")
        private String airlineName;
        @JsonProperty("AlternativeBusinessName")
        private String alternativeBusinessName;

        @Generated("by IDE")
        public AirlineCode getAirlineCode() {
            return airlineCode;
        }

        @Generated("by IDE")
        public AirlineEntry setAirlineCode(final AirlineCode airlineCode) {
            this.airlineCode = airlineCode;
            return this;
        }

        @Generated("by IDE")
        public String getAirlineName() {
            return airlineName;
        }

        @Generated("by IDE")
        public AirlineEntry setAirlineName(final String airlineName) {
            this.airlineName = airlineName;
            return this;
        }

        @Generated("by IDE")
        public String getAlternativeBusinessName() {
            return alternativeBusinessName;
        }

        @Generated("by IDE")
        public AirlineEntry setAlternativeBusinessName(final String alternativeBusinessName) {
            this.alternativeBusinessName = alternativeBusinessName;
            return this;
        }
    }
}
