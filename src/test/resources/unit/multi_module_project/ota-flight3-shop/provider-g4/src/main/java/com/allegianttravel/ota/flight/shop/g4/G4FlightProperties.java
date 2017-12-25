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

import com.allegiant.util.property.PropertySetEnum;

public enum G4FlightProperties implements PropertySetEnum {
    G4_FLIGHTS_SHOP_ENDPOINT("ota-flight3-shop-provider.g4.endpoint", ""),
    G4_OTA_MAINT_ENDPOINT("ota-flight3-shop-provider.g4.otamaint.endpoint", ""),
    G4_AIRPORTS("ota-flight3-shop-provider.g4.endpoint.airports", ""),
    G4_FARE_MAKER("ota-flight3-shop-provider.g4.faremaker.endpoint", ""),
    G4_FARE_MAKER_ENABLED("ota-flight3-shop-provider.g4.faremaker.endpoint.enabled", "true"),
    G4_FLIGHTS_ANCILLARY_SERVICE_ENDPOINT("ota-flight3-shop-provider.g4.ancillary.endpoint", ""),
    G4_FLIGHTS_ANCILLARY_SERVICE_ENABLED("ota-flight3-shop-provider.g4.ancillary.enabled", "true"),
    G4_SWIM_LANES("ota-flight3-shop-provider.g4.swimlanes", "STD=1,FLEX=2"),
    G4_SWIM_LANES_CACHE("ota-flight3-shop-provider.g4.swimlanes.cache", "10");

    private final String key;
    private final String def;

    G4FlightProperties(String key, String def) {
        this.key = key;
        this.def = def;
    }

    public String getKey() {
        return key;
    }

    public String getValueDefault() {
        return def;
    }
}

