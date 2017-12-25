/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegiant.tests;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public final class LoadFromClasspath {

    private LoadFromClasspath() {}

    public static String asString(final String path) throws IOException {
        return IOUtils.toString(asStream(path), "UTF-8");
    }

    @SuppressWarnings("WeakerAccess")
    public static InputStream asStream(final String path) {
        return LoadFromClasspath.class.getResourceAsStream(path);
    }
}

