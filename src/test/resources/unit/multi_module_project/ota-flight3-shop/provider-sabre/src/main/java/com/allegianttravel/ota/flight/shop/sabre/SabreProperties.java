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

import com.allegiant.util.property.PropertySetEnum;

import javax.annotation.Generated;

public enum SabreProperties implements PropertySetEnum {
    ENDPOINT("ota-flight3-shop-provider.sabre.endpoint", "https://api.test.sabre.com/"),
    API_CLIENTID("ota-flight3-shop-provider.sabre.api_clientId", "VjE6YnE3eTY5OG85NnMxNWthYTpERVZDRU5URVI6RVhU"),
    API_SECRET("ota-flight3-shop-provider.sabre.api_secret", "b1VVTnRnNzY=");

    private final String key;
    private final String valueDefault;

    SabreProperties(String key, String valueDefault) {
        this.key = key;
        this.valueDefault = valueDefault;
    }

    @Override
    @Generated("by IDE")
    public String getKey() {
        return key;
    }

    @Override
    @Generated("by IDE")
    public String getValueDefault() {
        return valueDefault;
    }
}
