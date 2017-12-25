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

import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PackageSupplier implements Supplier<PackageId> {
    private final String name;
    private final AtomicInteger counter = new AtomicInteger();

    public PackageSupplier(String name) {
        this.name = name;
    }

    @Override
    public PackageId get() {
        return new PackageId(name + counter.getAndIncrement());
    }
}
