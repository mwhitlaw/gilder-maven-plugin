/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.faremaker;

import com.allegianttravel.ota.faremaker.FareClassQualifiedItem;
import com.allegianttravel.ota.faremaker.NewFare;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight3.pricing.BundledFareItem;
import com.allegianttravel.ota.flight3.pricing.BundledFareItemType;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.allegianttravel.ota.flight3.pricing.Tax;
import com.allegianttravel.ota.flight3.pricing.TaxCalculation;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FlightPriceAdjusterTest {

    @Test
    public void computeBaseFarePrice_dawnsNumbers() throws Exception {
        FlightPriceAdjuster.Adjustment result = FlightPriceAdjuster.computeBaseFarePrice(
                Money.dollars(74.23), // STD base fare
                Money.dollars(5.57),  // STD XT
                Money.dollars(14),    // TripFlex
                Money.dollars(1), // TripFlex discount
                new BigDecimal(.075) // XT
        );
        assertEquals(Money.dollars(72.33), result.getBaseFare());
    }

    @Test
    public void computeBaseFarePrice_dawnsNumbers2() throws Exception {
        FlightPriceAdjuster.Adjustment result = FlightPriceAdjuster.computeBaseFarePrice(
                Money.dollars(12.84), // STD base fare
                Money.dollars(0.96),  // STD XT
                Money.dollars(13),    // TripFlex
                Money.dollars(1), // TripFlex discount
                new BigDecimal(.075) // XT
        );
        assertEquals(Money.dollars(11.00), result.getBaseFare());
    }

    @Test
    public void computeBaseFarePrice_dawnsNumbers3() throws Exception {
        FlightPriceAdjuster.Adjustment result = FlightPriceAdjuster.computeBaseFarePrice(
                Money.dollars(67.72), // STD base fare
                Money.dollars(5.08),  // STD XT
                Money.dollars(13),    // TripFlex
                Money.dollars(1), // TripFlex discount
                new BigDecimal(.075) // XT
        );
        assertEquals(Money.dollars(65.88), result.getBaseFare());
    }

    @Test
    public void createNamedFare_nonTaxable() throws Exception {
        FareDetails fareDetails = createFareDetails();
        NewFare newFare = new NewFare().setName("FOO").setFareItems(
                Collections.singletonList(
                new FareClassQualifiedItem().setFareItem(
                    new BundledFareItem().setItemType(BundledFareItemType.BAG)
                    .setTaxable(false)
                    .setCharge(Money.dollars(50))
                        )
                .setFareClasses(Collections.singleton("Y"))));
        NamedFare source = new NamedFare(ProviderFares.STD_RATE, Collections.singletonList(fareDetails),
                new PassengerTypeCount(PassengerType.ADULT, 1));
        NamedFare namedFare = G4PackageDecorator.createNamedFare(source, "Y", newFare);
        assertEquals("FOO", namedFare.getName());
        assertEquals(1, namedFare.getFareDetails().size());
        assertEquals("base fare shouldn't have changed since the item wasn't taxable", Money.dollars(100d), namedFare.getFareDetails().get(0).getBaseFare());
        assertEquals("excise tax shouldn't have changed since the bundled item wasn't taxable",
                Money.dollars(10d), namedFare.getFareDetails().get(0).getTaxTotal());
    }

    private FareDetails createFareDetails() throws Exception {
        Money baseFare = Money.dollars(100d);
        TaxCalculation fakeExciseTax = new TaxCalculation()
                .setAmount(new BigDecimal("0.10"))
                .setType(TaxCalculation.Type.PERCENTAGE);
        FareDetails fareDetails = new FareDetails()
                .setApplicablePassengerTypes(Collections.singletonList(PassengerType.ADULT))
                .setBaseFare(baseFare)
                .setTaxes(Collections.singletonList(
                        new Tax()
                                .setAmount(baseFare.multiply(fakeExciseTax.getAmount()))
                                .setCalculation(fakeExciseTax)));
        return fareDetails;
    }

}
