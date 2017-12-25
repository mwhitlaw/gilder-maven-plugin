/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre.lookup;

import com.allegianttravel.ota.flight.shop.rest.dto.travel.AircraftCode;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

public class SabreAircraftResponse {

    @JsonProperty("AircraftInfo")
    private List<AircraftEntry> aircraftInfo;

    @Generated("by IDE")
    public List<AircraftEntry> getAircraftInfo() {
        return aircraftInfo;
    }

    @Generated("by IDE")
    public SabreAircraftResponse setAircraftInfo(final List<AircraftEntry> aircraftInfo) {
        this.aircraftInfo = aircraftInfo;
        return this;
    }

    public static class AircraftEntry {
        @JsonProperty("AircraftCode")
        private AircraftCode aircraftCode;
        @JsonProperty("AircraftName")
        private String aircraftName;

        @Generated("by IDE")
        public AircraftCode getAircraftCode() {
            return aircraftCode;
        }

        @Generated("by IDE")
        public AircraftEntry setAircraftCode(final AircraftCode aircraftCode) {
            this.aircraftCode = aircraftCode;
            return this;
        }

        @Generated("by IDE")
        public String getAircraftName() {
            return aircraftName;
        }

        @Generated("by IDE")
        public AircraftEntry setAircraftName(final String aircraftName) {
            this.aircraftName = aircraftName;
            return this;
        }
    }
}
