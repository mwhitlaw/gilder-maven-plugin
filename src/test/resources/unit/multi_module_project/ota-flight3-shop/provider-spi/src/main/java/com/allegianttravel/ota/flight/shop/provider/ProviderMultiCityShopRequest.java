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

import com.allegianttravel.ota.flight.shop.rest.dto.travel.MultiCityShopRequest;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;

import javax.annotation.Generated;

public class ProviderMultiCityShopRequest extends ProviderInput {

    private final MultiCityShopRequest multiCityShopRequest;
    private final CallerMetadata callerMetadata;

    public ProviderMultiCityShopRequest(MultiCityShopRequest multiCityShopRequest, CallerMetadata callerMetadata) {
        this.multiCityShopRequest = multiCityShopRequest;
        this.callerMetadata = callerMetadata;
    }

    @Generated("by IDE")
    public MultiCityShopRequest getMultiCityShopRequest() {
        return multiCityShopRequest;
    }

    @Generated("by IDE")
    public CallerMetadata getCallerMetadata() {
        return callerMetadata;
    }
}
