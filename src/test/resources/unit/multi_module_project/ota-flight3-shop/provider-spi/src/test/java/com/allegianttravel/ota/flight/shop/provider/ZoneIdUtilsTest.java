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

import org.junit.Test;

import java.time.ZoneId;

import static com.allegianttravel.ota.flight.shop.provider.ZoneIdUtils.toZoneId;
import static org.junit.Assert.assertEquals;

public class ZoneIdUtilsTest {
    @Test
    public void zoneIdFromOffset() throws Exception {
        ZoneId zoneId = toZoneId(-4);
        assertEquals("-04:00", zoneId.toString());
    }

    @Test
    public void zoneIdFromOffset_fractional() throws Exception {
        ZoneId zoneId = toZoneId(-4.5);
        assertEquals("-04:30", zoneId.toString());
    }
}
