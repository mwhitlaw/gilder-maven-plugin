/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4;

import com.allegiant.util.property.PropertySetEnum;
import com.allegiant.util.property.PropertySetEnumProvider;

import java.util.Collections;
import java.util.List;

public class G4PropertiesProvider implements PropertySetEnumProvider {
    @Override
    public List<Class<? extends PropertySetEnum>> listPropertyClasses() {
        return Collections.singletonList(G4FlightProperties.class);
    }
}
