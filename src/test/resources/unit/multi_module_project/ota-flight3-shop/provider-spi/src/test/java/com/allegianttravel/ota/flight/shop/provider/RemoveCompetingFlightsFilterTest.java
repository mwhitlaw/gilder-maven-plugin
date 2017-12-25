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
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight3.pricing.Money;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.allegianttravel.ota.flight.shop.provider.FilterFixture.createFlightOption;
import static org.assertj.core.api.Assertions.assertThat;

public class RemoveCompetingFlightsFilterTest {

    private final RemoveCompetingFlightsFilter filter = new RemoveCompetingFlightsFilter();

    @Test
    public void hasConflicts_noPackages() throws Exception {
        // the one from spirit should get removed
        FlightKey bliLax_420_spirit = new FlightKey(
                new AirlineCode("NK"),
                new FlightNumber("123"),
                LocalDate.parse("2017-04-20")
        );

        FlightKey bliLax_420_g4_1 = new FlightKey(
                AirlineCode.G4,
                new FlightNumber("820"),
                LocalDate.parse("2017-04-20")
        );
        FlightKey bliLax_420_g4_2 = new FlightKey(
                AirlineCode.G4,
                new FlightNumber("825"),
                LocalDate.parse("2017-04-20")
        );

        FlightOption sabreOption = createFlightOption(FilterFixture.BLI, FilterFixture.LAX, bliLax_420_spirit, Money.dollars(123d), "sabre", PackageId.NONE);
        FlightOption g4One = createFlightOption(FilterFixture.BLI, FilterFixture.LAX, bliLax_420_g4_1, Money.dollars(65d), "g4", PackageId.NONE);
        FlightOption g4Two = createFlightOption(FilterFixture.BLI, FilterFixture.LAX, bliLax_420_g4_2, Money.dollars(62d), "g4", PackageId.NONE);

        FlightOptions options = new FlightOptions().setFlightOptions(Arrays.asList(sabreOption, g4One, g4Two));

        List<FlightOption> removed = filter.removeCompetingFlights(options);

        assertThat(removed).containsExactly(sabreOption);
        assertThat(options.getFlightOptions()).containsExactlyInAnyOrder(g4One, g4Two);
    }

}
