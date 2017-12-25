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

import java.util.List;

public final class CsvQueryParam {

    private CsvQueryParam() {}

    public static String toCSV(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return "";
        } else {
            return String.join(",", codes);
        }
    }

}
