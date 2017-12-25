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

import java.time.ZoneId;
import java.time.ZoneOffset;

public final class ZoneIdUtils {
    private static final int SECONDS_IN_HOUR = 60 * 60;

    private ZoneIdUtils() {

    }

    /**
     * Computes the ZoneId from a double which is the number of hours for the offset.
     * @param gmtOffset
     * @return
     */
    public static ZoneId toZoneId(double gmtOffset) {

        return ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds((int) (gmtOffset * SECONDS_IN_HOUR)));
    }

}
