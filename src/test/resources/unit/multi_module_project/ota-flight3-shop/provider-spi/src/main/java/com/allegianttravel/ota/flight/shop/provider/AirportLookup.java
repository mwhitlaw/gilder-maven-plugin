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

import com.allegianttravel.ota.flight.shop.rest.dto.travel.Airport;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;

public interface AirportLookup extends LookupService<AirportCode, Airport> {
}
