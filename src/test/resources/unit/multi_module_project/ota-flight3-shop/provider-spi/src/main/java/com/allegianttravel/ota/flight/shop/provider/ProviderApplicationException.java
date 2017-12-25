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

public class ProviderApplicationException extends RuntimeException {

    public ProviderApplicationException(String message) {
        super(message);
    }

    public ProviderApplicationException(Exception e) {
        super(e);
    }
}
