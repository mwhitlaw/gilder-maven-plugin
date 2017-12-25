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
import com.allegianttravel.ota.faremaker.FareMakerResults;
import com.allegianttravel.ota.faremaker.NewFare;
import com.allegianttravel.ota.faremaker.NewFaresResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.price.AppliedBundledFareItem;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight3.pricing.BundledFareItem;
import com.allegianttravel.ota.flight3.pricing.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class will decorate the G4 results to add packages based on the existing fare. The packages come from the
 * fare maker results.
 */
final class G4PackageDecorator {

    private static final Logger logger = LoggerFactory.getLogger(G4PackageDecorator.class);

    private G4PackageDecorator() {}

    /**
     * Walks all of the flights and adds new fares for dates of travel from the fare maker search results
     * @param flightOption
     */
    static void decorate(FlightOption flightOption, FareMakerResults fareMakerResults, Predicate<NewFare> filter) {

        // this flight should only be offered by G4
        assert flightOption.getProviderFares().size() == 1;

        ProviderFares providerFares = flightOption.getProviderFares().get(0);
        assert "g4".equals(providerFares.getProviderId());

        // currently there's only a single fare presented by G4.
        assert providerFares.getNamedFares().size() == 1;

        NamedFare standardRateFare = providerFares.getLowestNamedFare();

        // assuming g4 only has a single fare class for all segments
        String fareClassCode = providerFares.getFareClassCodes().get(0);

        LocalDate dayOfTravel = flightOption.getFirstDayOfTravel();
        NewFaresResponse newFares = fareMakerResults.getResponsesByDate().get(dayOfTravel);

        if (newFares != null) {

            newFares.getFares().stream().filter(filter).forEach(newFare -> {
                NamedFare namedFare = createNamedFare(standardRateFare, fareClassCode, newFare);
                Money namedFareTotal = namedFare.getFareSalesTotal();

                providerFares.addNamedFare(namedFare);

                logger.trace("computed new fare with sales total of {} and a strikethru of {}",
                        namedFareTotal, namedFare.getStrikethru());
            });
        }
    }

    // default access for testing
    static NamedFare createNamedFare(NamedFare source, String fareClassCode, NewFare newFare) {
        List<FareDetails> adjusted = source.getFareDetails()
                .stream()
                .map(fareDetails ->
                        FlightPriceAdjuster.copyWithAdjustedBaseFare(fareDetails,
                                getTaxableTotal(newFare, fareClassCode),
                                newFare.getTotalDiscount(fareClassCode)))
                .map(fareDetails -> fareDetails.setBundledItems(newFare.getFareItems()
                        .stream()
                        .filter(fareClassQualifiedItem -> fareClassQualifiedItem.getFareClasses().contains(fareClassCode))
                        .map(FareClassQualifiedItem::getFareItem)
                        .map(AppliedBundledFareItem::applyDiscountToBaseBare)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
        NamedFare namedFare = new NamedFare(newFare.getName(), adjusted, source.getPassengerTypeCounts());
        namedFare.setOriginalRatePlanCodes(source.getRatePlanCodes());
        if (newFare.getRatePlan() != null) {
            // this is odd because G4 only has a single segment but other providers can have
            // multiple segments in a flight where each segment is from a different bucket
            namedFare.setRatePlanCodes(source.getRatePlanCodes()
                    .stream()
                    .map(code -> fareClassCode + newFare.getRatePlan())
                    .collect(Collectors.toList()));
        } else {
            namedFare.setRatePlanCodes(source.getRatePlanCodes());
        }
        return namedFare;
    }

    private static Money getTaxableTotal(NewFare newFare, String fareClassCode) {
        // our interpretation of the taxable total differs from NewFare::getTaxableTotal
        // we want to bake the discounts (if any) into the base fare which means the taxable
        // amount of the items is actually the full amount since the calc for the base fare
        // also includes the discounts
        return newFare.getFareItems().stream()
                .filter(fareClassQualifiedItem -> fareClassQualifiedItem.appliesTo(fareClassCode))
                .map(FareClassQualifiedItem::getFareItem)
                .filter(BundledFareItem::isTaxable)
                .map(bundledFareItem -> bundledFareItem.getCharge().add(bundledFareItem.getDiscountedAmount()))
                .reduce(Money.ZERO, Money::add);

    }

}
