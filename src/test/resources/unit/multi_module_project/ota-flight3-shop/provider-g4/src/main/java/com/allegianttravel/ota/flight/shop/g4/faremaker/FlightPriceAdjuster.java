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

import com.allegianttravel.ota.flight.shop.rest.dto.price.BaseFareFromAirline;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.allegianttravel.ota.flight3.pricing.Tax;
import com.allegianttravel.ota.flight3.pricing.TaxCalculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Generated;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FlightPriceAdjuster {

    private static final Logger logger = LoggerFactory.getLogger(FlightPriceAdjuster.class);

    private FlightPriceAdjuster() {}

    static FareDetails copyWithAdjustedBaseFare(FareDetails source, Money taxableItems, Money discountAmount) {

        FareDetails fareDetails = FareDetails.copyOf(source);

        if (!source.getBaseFareFromAirline().isPresent()) {
            // since we're changing the base fare, we should record the fare we got from the airline so we can get back
            // to the original fare for the purposes of a strikethru
            fareDetails.setBaseFareFromAirline(new BaseFareFromAirline()
                    .setStrikethruBaseFare(source.getBaseFare())
                    .setStrikethruTax(source.getTaxTotal())
                    .setDiscountsApplied(Collections.emptyList()));
        }

        // compute the new base fare
        Adjustment adjustment = computeBaseFarePrice(fareDetails.getBaseFare(),
                fareDetails.getTaxTotal(),
                taxableItems,
                discountAmount,
                fareDetails.getTaxRate());

        fareDetails.setBaseFare(adjustment.getBaseFare());

        // new XT = originalBaseFare + originalXT - newBaseFare - bundleItemDiscounts
        // newBaseFare is being lowered by the additional XT you're paying for the bundled items
        Money adjustedXT = source.getBaseFare()
                .add(source.getTaxTotal())
                .subtract(fareDetails.getBaseFare())
                .subtract(adjustment.getDiscountAmount());

        List<Tax> taxes = fareDetails.getTaxes();
        List<Tax> computedTaxes = adjustTaxes(taxes, adjustedXT);
        fareDetails.setTaxes(computedTaxes);

        assert fareDetails.getTaxTotal().equals(adjustedXT);
        return fareDetails;
    }

    /**
     * Returns a list of taxes with the new excise tax applied. In the case of a flex fare with bundled items we lower
     * the base fare by the amount that the customer must pay in excise tax for the items.
     *
     * Note: While the model supports multiple taxes that are percentages, we're not currently supporting that.
     * Federal Excise Tax is currently the only percentage based tax we apply. If we were to apply multiple taxes then
     * we need to know whether they are compounding (which seems awful) or accumulate and are applied once
     * (which seems sane).
     *
     * @param taxes
     * @param adjustedExciseTax
     * @return
     */
    private static List<Tax> adjustTaxes(List<Tax> taxes, Money adjustedExciseTax) {
        List<Tax> computedTaxes = new ArrayList<>();
        int pctCount = 0;
        for (Tax tax : taxes) {
            TaxCalculation calc = tax.getCalculation();
            switch (calc.getType()) {
                case PERCENTAGE:
                    // we don't support multiple percentages. We could support multiple pct's by
                    // distributing the adjusted tax burden
                    assert pctCount == 0;
                    pctCount++;
                    computedTaxes.add(
                            new Tax().setCalculation(tax.getCalculation())
                                    .setCode(tax.getCode())
                                    .setDescription(tax.getDescription())
                                    .setAmount(adjustedExciseTax)
                    );
                    break;
                case FIXED:
                default:
                    computedTaxes.add(tax);
                    break;
            }
        }
        return computedTaxes;
    }

    static Adjustment computeBaseFarePrice(Money originalBaseFare,
                                           Money originalExciseTax,
                                           Money undiscountedBundledItems,
                                           Money itemDiscount,
                                           BigDecimal taxRate) {

        Money pass1 = makePass(originalBaseFare, originalExciseTax, undiscountedBundledItems, itemDiscount, taxRate);

        int comparison = pass1.compareTo(itemDiscount);

        if (comparison <= 0) {
            logger.trace("Making second pass because the computed base fare of {} is less than the discount amount of " +
                    "{} and fares can't be negative", pass1, itemDiscount);
            return new Adjustment(makePass(originalBaseFare, originalExciseTax, undiscountedBundledItems,
                    pass1.floor(), taxRate), pass1.floor());
        } else {
            return new Adjustment(pass1, itemDiscount);
        }
    }

    static class Adjustment {
        private final Money baseFare;
        private final Money discountAmount;

        Adjustment(Money baseFare, Money discountAmount) {
            this.baseFare = baseFare;
            this.discountAmount = discountAmount;
        }

        @Generated("by IDE")
        public Money getBaseFare() {
            return baseFare;
        }

        @Generated("by IDE")
        public Money getDiscountAmount() {
            return discountAmount;
        }
    }

    private static Money makePass(Money originalBaseFare, Money originalExciseTax, Money undiscountedBundledItems,
                                  Money itemDiscount, BigDecimal taxRate) {
        return originalBaseFare.add(originalExciseTax)
                .add(undiscountedBundledItems)
                .subtract(itemDiscount)
                .divide(BigDecimal.ONE.add(taxRate), new MathContext(7, RoundingMode.HALF_UP))
                .subtract(undiscountedBundledItems);
    }

}
