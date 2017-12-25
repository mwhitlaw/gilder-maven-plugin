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
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightShopResponse;
import com.allegianttravel.ota.flight3.pricing.Money;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.allegianttravel.ota.flight.shop.provider.FilterFixture.createFlightOption;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class RemoveCompetingFlightsFilter_PackageTest {
    private static final AirportCode BLI = new AirportCode("BLI");
    private static final AirportCode LAX = new AirportCode("LAX");
    private final RemoveCompetingFlightsFilter filter = new RemoveCompetingFlightsFilter();

    // Spirit can do a round trip. The outbound flight doesn't conflict so it should stay
    private FlightKey bliLax_420_spirit = new FlightKey(
            new AirlineCode("NK"),
            new FlightNumber("123"),
            LocalDate.parse("2017-04-20")
    );

    // the return flight conflicts with a G4 one so it should be removed
    private FlightKey laxBli_422_spirit = new FlightKey(
            new AirlineCode("NK"),
            new FlightNumber("123"),
            LocalDate.parse("2017-04-22")
    );

    // suppose G4 can only provide the return flight
    private FlightKey laxBli_422_g4 = new FlightKey(
            AirlineCode.G4,
            new FlightNumber("825"),
            LocalDate.parse("2017-04-22")
    );

    private FlightRequest outboundFlightKey = new FlightRequest(
            BLI,
            LAX,
            bliLax_420_spirit.getDeparture());
    private FlightRequest returnFlightKey = new FlightRequest(
            LAX,
            BLI,
            laxBli_422_g4.getDeparture());

    private FlightShopResponse flightShopResponse = new FlightShopResponse();

    private FlightOption bliLax_420_spiritOption;
    @SuppressWarnings("FieldCanBeLocal")
    private FlightOption laxBli_422_spiritOption;

    private FlightOption laxBli_422_g4Option;

    @Before
    public void setup() throws Exception {
        // package deal from sabre for the Spirit flight
        bliLax_420_spiritOption = createFlightOption(FilterFixture.BLI, FilterFixture.LAX, bliLax_420_spirit,
                Money.dollars(123d), "sabre", new PackageId("sabre-1"));
        laxBli_422_spiritOption = createFlightOption(FilterFixture.LAX, FilterFixture.BLI, laxBli_422_spirit,
                Money.dollars(95d), "sabre", new PackageId("sabre-1"));

        laxBli_422_g4Option = createFlightOption(FilterFixture.LAX, FilterFixture.BLI, laxBli_422_g4,
                Money.dollars(65d), "g4", PackageId.NONE);

        Map<FlightRequest, FlightOptions> map = new HashMap<>();


        map.put(outboundFlightKey,
                new FlightOptions().setFlightOptions(Collections.singletonList(bliLax_420_spiritOption)));

        map.put(returnFlightKey,
                new FlightOptions().setFlightOptions(Arrays.asList(laxBli_422_spiritOption, laxBli_422_g4Option)));

        flightShopResponse.setFlightOptions(map);
    }

    /**
     * Tests the scenario where an return flight conflicts with a G4 flight
     * so we need to remove the outbound flight which is part of the package.
     * @throws Exception
     */
    @Test
    public void hasConflicts_conflictingReturn() throws Exception {

        filter.process(flightShopResponse);

        assertThat(flightShopResponse.getFlightOptions().get(outboundFlightKey).getFlightOptions()).isEmpty();
        assertThat(flightShopResponse.getFlightOptions().get(returnFlightKey).getFlightOptions())
                .containsExactly(laxBli_422_g4Option);
    }

    /**
     * Same scenario but now the competing flight is also part of a one-way pricing option so the outbound isn't removed
     * @throws Exception
     */
    @Test
    public void hasConflicts_conflictingKeepTheUnpackagedFlight() throws Exception {

        // add a one-way pricing option
        bliLax_420_spiritOption.getProviderFares().add(
                new ProviderFares()
                        .setPackageId(PackageId.NONE)
                        .setProviderId("sabre")
                        .addNamedFare(new NamedFare("RACK", Collections.singletonList(
                                new FareDetails().setBaseFare(Money.dollars(88d))),
                                new PassengerTypeCount(PassengerType.ADULT, 1))));

        filter.process(flightShopResponse);

        assertThat(flightShopResponse.getFlightOptions().get(outboundFlightKey).getFlightOptions())
                .containsExactly(bliLax_420_spiritOption);
        assertThat(flightShopResponse.getFlightOptions().get(returnFlightKey).getFlightOptions())
                .containsExactly(laxBli_422_g4Option);
        assertEquals(1, bliLax_420_spiritOption.getProviderFares().size());
        assertEquals(PackageId.NONE, bliLax_420_spiritOption.getProviderFares().get(0).getPackageId());
    }

}
