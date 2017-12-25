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

import com.allegianttravel.ota.flight.shop.rest.dto.FlightKey;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight3.pricing.Money;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.allegianttravel.ota.flight.shop.provider.FilterFixture.createFlightOption;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class MergeFlightOptionsTest {

    private FlightKey BOS_LAS = new FlightKey(
            new AirlineCode("NK"),
            new FlightNumber("123"),
            LocalDate.parse("2017-04-20")
    );

    private FlightKey BOS_LAS_2 = new FlightKey(
            new AirlineCode("NK"),
            new FlightNumber("888"),
            LocalDate.parse("2017-04-20")
    );

    private FlightKey LAS_BOS = new FlightKey(
            new AirlineCode("NK"),
            new FlightNumber("456"),
            LocalDate.parse("2017-04-25")
    );


    /**
     * Single provider with only one option. There is no need for a merge here.
     */
    @Test
    public void single_provider_no_merge() throws Exception {
        FlightOption option = createFlightOption(FilterFixture.BOS, FilterFixture.LAS, BOS_LAS, Money.dollars(124.20), "g4", PackageId.NONE
        );
        FlightOptions actual = MergeFlightOptions.merge(
                Collections.singletonList(
                        new FlightOptions()
                                .setFlightOptions(Collections.singletonList(option))
                ));

        assertThat(actual.getFlightOptions()).containsExactly(option);
    }

    /**
     * Single provider that has two options that should be merged.
     *
     * The likely explanation for the merge is that the options have different
     * return flights.
     */
    @Test
    public void single_provider_with_merge() throws Exception {
        PackageId packA = new PackageId("a");
        FlightOption option1 = createFlightOption(
                FilterFixture.BOS, FilterFixture.LAS,
                BOS_LAS, Money.dollars(124.20), "qpx", packA
        );
        PackageId packB = new PackageId("b");
        FlightOption option2 = createFlightOption(
                FilterFixture.LAS, FilterFixture.BOS,
                BOS_LAS, Money.dollars(234.20), "qpx", packB
        );
        FlightOptions actual = MergeFlightOptions.merge(
                Collections.singletonList(
                        new FlightOptions()
                                .setFlightOptions(Arrays.asList(option1, option2))
                ));

        assertEquals(1, actual.getFlightOptions().size());
        assertThat(actual.getFlightOptions().get(0).getProviderFares()).extracting("packageId").contains(packA, packB);
    }

    /**
     * Sabre provides multiple options and QPX provides a single. We remove QPX based on the simple logic
     * that we came across Sabre first.
     */
    @Test
    public void multiple_provider_dupeRemoval() throws Exception {
        AirportCode BOS = new AirportCode("BOS");
        AirportCode LAS = new AirportCode("LAS");
        // Sabre has 2 outbound flight options but only 1 inbound option
        PackageId packB = new PackageId("sabre-b");
        FlightOption outound = createFlightOption(
                FilterFixture.BOS, FilterFixture.LAS,
                BOS_LAS, Money.dollars(234.20), "sabre", packB);
        PackageId packC = new PackageId("sabre-c");
        FlightOption outound2 = createFlightOption(
                FilterFixture.BOS, FilterFixture.LAS,
                BOS_LAS_2, Money.dollars(240), "sabre", packC);
        FlightOption inbound = createFlightOption(
                FilterFixture.LAS, FilterFixture.BOS,
                LAS_BOS, Money.ZERO, "sabre", packB);
        inbound.getProviderFares().add(new ProviderFares().setProviderId("sabre").setPackageId(packC));

        // qpx offers the same outbound/inbound flight combo as sabre's package B
        PackageId packA = new PackageId("qpx-a");
        outound.getProviderFares().add(new ProviderFares().setPackageId(packA).setProviderId("qpx"));
        inbound.getProviderFares().add(new ProviderFares().setPackageId(packA).setProviderId("qpx"));

        Map<FlightRequest, FlightOptions> optionsByRequest = new LinkedHashMap<>();
        FlightRequest outboundRequest = new FlightRequest(BOS, LAS, BOS_LAS.getDeparture());
        optionsByRequest.put(outboundRequest,
                new FlightOptions().setFlightOptions(Arrays.asList(outound, outound2)));

        FlightRequest inboundRequest = new FlightRequest(LAS, BOS, LAS_BOS.getDeparture());
        optionsByRequest.put(inboundRequest,
                new FlightOptions().setFlightOptions(Collections.singletonList(inbound)));

        MergeFlightOptions.removeDupes(optionsByRequest);
        assertEquals(2, optionsByRequest.size());
        FlightOptions outboundOptions = optionsByRequest.get(outboundRequest);
        assertEquals(outboundOptions.getFlightOptions().get(0).getProviderFares().get(0).getPackageId(), packB);
        assertEquals(outboundOptions.getFlightOptions().get(1).getProviderFares().get(0).getPackageId(), packC);

        FlightOptions inboundOptions = optionsByRequest.get(inboundRequest);
        assertThat(inboundOptions.getFlightOptions().get(0).getProviderFares()).extracting("packageId").containsExactlyInAnyOrder(packB, packC);
    }
}
