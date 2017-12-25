/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.ancillary;

import com.allegiant.commons.json.JsonUtils;
import com.allegiantair.g4flights.ancillary.service.rest.AncillaryFeesByDateResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import org.junit.Test;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

public class G4AncillaryUtilTest {

    @Test
    public void test() throws Exception {
        // load the response payload
        // map to our type
        // assert results

        AncillaryFeesByDateResponse feesByDateResponse = JsonUtils.fromJson(
                asString("/multidate-fees-response.json"),
                AncillaryFeesByDateResponse.class);

        FlightFees flightFees = G4AncillaryUtil.mapFromG4Ancillary(feesByDateResponse);

        String actual = JsonUtils.toJson(flightFees);
//        System.out.println(actual);
        assertJsonEquals(asString("/expected-flight-fees.json"), actual);

    }

}
