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

import com.allegiant.util.gen2.JsonUtils;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AddConnectionInfoFilterTest {
    @Test
    public void test() throws Exception {
        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        FlightOption fo = om.readValue(asString("/flightOption-connectionInfo.json"), FlightOption.class);

        // none of the legs have connection infos
        Optional<Leg> any = fo.getSegments().stream().flatMap(seg -> seg.getLegs().stream()).filter(leg -> leg.getConnectionInfo() != null).findAny();
        assertFalse(any.isPresent());

        AddConnectionInfoFilter filter = new AddConnectionInfoFilter();
        filter.decorate(fo);

        // all but the first leg have connection info
        any = fo.getSegments().stream().flatMap(seg -> seg.getLegs().stream()).skip(1).filter(leg -> leg.getConnectionInfo() != null).findAny();
        assertTrue(any.isPresent());

        // assert the expected connection info
        List<Integer> actual = fo.getSegments().stream().flatMap(seg -> seg.getLegs().stream()).skip(1).map(leg -> leg.getConnectionInfo().getConnectionDuration()).collect(Collectors.toList());

        assertThat(actual).containsExactly(130);
    }
}
