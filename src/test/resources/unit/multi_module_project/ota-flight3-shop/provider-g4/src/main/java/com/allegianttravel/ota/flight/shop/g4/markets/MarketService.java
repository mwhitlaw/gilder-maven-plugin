/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.markets;


import com.allegianttravel.ota.flight3.MarketName;

public interface MarketService {
    boolean isAllowed(MarketName marketName);
}
