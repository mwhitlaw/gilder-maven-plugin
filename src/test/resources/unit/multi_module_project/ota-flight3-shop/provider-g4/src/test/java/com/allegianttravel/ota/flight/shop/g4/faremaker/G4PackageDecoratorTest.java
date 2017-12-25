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

import com.allegiant.util.gen2.JsonUtils;
import com.allegianttravel.ota.faremaker.FareClassQualifiedItem;
import com.allegianttravel.ota.faremaker.FareMakerResults;
import com.allegianttravel.ota.faremaker.NewFare;
import com.allegianttravel.ota.faremaker.NewFaresResponse;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.price.BaseFareFromAirline;
import com.allegianttravel.ota.flight.shop.rest.dto.price.DiscountReason;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight3.pricing.BundledFareItem;
import com.allegianttravel.ota.flight3.pricing.BundledFareItemType;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.annotation.Generated;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.allegiant.tests.LoadFromClasspath.asString;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class G4PackageDecoratorTest {

    private static final ObjectMapper OM = JsonUtils.defaultJacksonObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final DiscountReason ROUNDTRIP = new DiscountReason().setAmount(Money.dollars(9.30)).setName("ROUNDTRIP");
    private static final DiscountReason COUPON = new DiscountReason().setAmount(Money.dollars(13.95)).setName("COUPON");

    private final StdInputValues inputValues;
    private final List<NewFare> newFares;
    private final Map<String, ExpectedValues> expectedValuesMap;

    @SuppressWarnings("unused")
    public G4PackageDecoratorTest(String name, StdInputValues stdInputValues, List<NewFare> newFares, List<ExpectedValues> expectedValues) {
        this.inputValues = stdInputValues;
        this.expectedValuesMap = expectedValues.stream().collect(Collectors.toMap(ExpectedValues::getName, ev->ev));
        this.newFares = newFares;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() throws Exception {
        return Arrays.asList(
                new Object[]{
                        "one passenger",
                        new StdInputValues().setBaseFare(67.72).setXt(5.08),
                        Arrays.asList(createFlexFare(Money.dollars(12), Money.dollars(1)), createBundleFare()),
                        Arrays.asList(
                                new ExpectedValues()
                                        .setFareSalesTotal(99d)
                                        .setStrikethru(100d)
                                        .setBaseFare(65.88d)
                                        .setXt(5.92d),
                                new ExpectedValues()
                                        .setName("FLEX-BUNDLE")
                                        .setFareSalesTotal(167d)
                                        // bundle items in the test are discounted by $7
                                        .setStrikethru(167 + 7)
                                        .setBaseFare(55.14)
                                        .setXt(10.66d)
                                        // The Flex Bundle has a different rate plan code
                                        .setRatePlanCode("TBDL")
                        )
                },
                new Object[] {
                        "oneway",
                        new StdInputValues().setBaseFare(12.84).setXt(0.96),
                        Collections.singletonList(createFlexFare(Money.dollars(13), Money.dollars(1))),
                        Collections.singletonList(
                                new ExpectedValues()
                                        .setBaseFare(10.93)
                                        .setXt(1.87)
                                        .setFareSalesTotal(41d)
                                        .setStrikethru(42)
                        )
                },
                new Object[] {
                        "oneway-dawns",
                        new StdInputValues().setBaseFare(74.23).setXt(5.57),
                        Collections.singletonList(createFlexFare(Money.dollars(13), Money.dollars(1))),
                        Collections.singletonList(
                                new ExpectedValues()
                                        .setBaseFare(72.33)
                                        .setXt(6.47)
                                        .setFareSalesTotal(107d)
                                        .setStrikethru(108d)
                        )
                },
                new Object[] {
                        "roundtrip",
                        new StdInputValues().setBaseFare(3.53).setXt(0.27)
                        .setBaseFareFromAirline(new BaseFareFromAirline()
                        .setStrikethruBaseFare(Money.dollars(12.84))
                        .setStrikethruTax(Money.dollars(0.96))
                        .setDiscountsApplied(Collections.singletonList(ROUNDTRIP))),
                        Collections.singletonList(createFlexFare(Money.dollars(13), Money.dollars(1))),
                        Collections.singletonList(
                                new ExpectedValues()
                                        .setBaseFare(1.63)
                                        .setXt(1.17)
                                        .setFareSalesTotal(31d)
                                        .setStrikethru(42d)
                        )
                },
                new Object[] {
                        "full monty",
                        new StdInputValues().setBaseFare(74.23).setXt(5.57)
                                .setBaseFareFromAirline(new BaseFareFromAirline()
                                .setStrikethruBaseFare(Money.dollars(97.49))
                                .setStrikethruTax(Money.dollars(7.31))
                                .setDiscountsApplied(Arrays.asList(ROUNDTRIP,COUPON)
                                )),
                        Collections.singletonList(createFlexFare(Money.dollars(14), Money.dollars(0))),
                        Collections.singletonList(
                                new ExpectedValues()
                                        .setBaseFare(73.26)
                                        .setXt(6.54)
                                        .setFareSalesTotal(108d)
                                        .setStrikethru(133)
                        )
                },
                new Object[] {
                        "nontaxable",
                        new StdInputValues().setBaseFare(12.84).setXt(0.96),
                        Collections.singletonList(createFlexFareNonTaxable(Money.dollars(13), Money.ZERO)),
                        Collections.singletonList(
                                new ExpectedValues()
                                        .setBaseFare(12.84)
                                        .setXt(0.96)
                                        .setFareSalesTotal(41d)
                                        .setStrikethru(null)
                        )
                }
                );
    }

    @Test
    public void test() throws Exception {
        String json = asString("/flightoption-shell.json");
        FlightOption flightOption = OM.readValue(json, FlightOption.class);

        // apply the input values to the std fare
        NamedFare stdFare = flightOption.getProviderFares().get(0).getLowestNamedFare();
        FareDetails fareDetails = stdFare.getFareDetails().get(0);
        fareDetails.setBaseFare(inputValues.getBaseFare());
        fareDetails.getTaxes().get(0).setAmount(inputValues.getXt());
        fareDetails.setBaseFareFromAirline(inputValues.getBaseFareFromAirline());

        List<NamedFare> namedFares = decorate(flightOption);

        String postDecoration = OM.writeValueAsString(flightOption);
        System.out.println(postDecoration);

        namedFares.stream().skip(1).forEach(namedFare -> {
            ExpectedValues ev = expectedValuesMap.get(namedFare.getName());

            assertEquals(ev.getName(), namedFare.getName());
            assertEquals("base fare didn't match for " + ev.getName(), ev.getBaseFare(), namedFare.getFareDetails().get(0).getBaseFare());
            assertEquals("xt didn't match for " + ev.getName(), ev.getXt(), namedFare.getFareDetails().get(0).getTaxTotal());
            assertEquals("fare sales total didn't match for " + ev.getName(), ev.getFareSalesTotal(), namedFare.getFareSalesTotal());
            assertEquals("strikethru didn't match for " + ev.getName(), ev.getStrikethru(), namedFare.getStrikethru());
            assertEquals("rate plan code doesn't match for " + ev.getName(), ev.getRatePlanCode(), namedFare.getRatePlanCodes().get(0));
        });

    }

    private List<NamedFare> decorate(FlightOption flightOption) {
        G4PackageDecorator.decorate(flightOption, new FareMakerResults(Collections.singletonList(
                new NewFaresResponse()
                        .setDepartureDate(LocalDate.parse("2017-07-23"))
                        .setFares(newFares)
        )), newFare -> true);

        List<NamedFare> namedFares = flightOption.getProviderFares().get(0).getNamedFares();
        assertEquals("expected to have a total of three fares", 1 + newFares.size(), namedFares.size());
        return namedFares;
    }

    private static NewFare createBundleFare() {
        return new NewFare()
                    .setName("FLEX-BUNDLE")
                    .setRatePlan("BDL")
                    .setFareItems(Arrays.asList(
                            new FareClassQualifiedItem(
                                new BundledFareItem()
                                        .setItemType(BundledFareItemType.TRIP_FLEX)
                                        .setCharge(Money.dollars(11d))
                                        .setDiscountedAmount(Money.dollars(2d)),
                                Collections.singleton("T")),
                            new FareClassQualifiedItem(
                                new BundledFareItem()
                                        .setItemType(BundledFareItemType.PRIORITY_BOARDING)
                                        .setCharge(Money.dollars(6d)),
                                Collections.singleton("T")),
                            new FareClassQualifiedItem(
                                new BundledFareItem()
                                        .setItemType(BundledFareItemType.CARRY_ON)
                                        .setCharge(Money.dollars(18d)),
                                    Collections.singleton("T")),
                            new FareClassQualifiedItem(
                                new BundledFareItem()
                                        .setItemType(BundledFareItemType.BAG)
                                        .setCharge(Money.dollars(45d))
                                        .setDiscountedAmount(Money.dollars(5d)),
                                    Collections.singleton("T"))
                    ));
    }

    private static NewFare createFlexFareNonTaxable(Money flexCharge, Money flexDiscount) {
        NewFare flexFare = createFlexFare(flexCharge, flexDiscount);
        flexFare.getFareItems().get(0).getFareItem().setTaxable(false);
        return flexFare;
    }

    private static NewFare createFlexFare(Money flexCharge, Money flexDiscount) {
        return new NewFare()
                    .setName("FLEX")
                .setRatePlan("FLX")
                    .setFareItems(Collections.singletonList(
                            new FareClassQualifiedItem(
                                new BundledFareItem()
                                        .setItemType(BundledFareItemType.TRIP_FLEX)
                                        .setCharge(flexCharge)
                                        .setDiscountedAmount(flexDiscount),
                                Collections.singleton("T"))));
    }

    static class StdInputValues {
        private Money baseFare;
        private Money xt;
        private BaseFareFromAirline baseFareFromAirline;

        @Generated("by IDE")
        public Money getBaseFare() {
            return baseFare;
        }

        @Generated("by IDE")
        public StdInputValues setBaseFare(Money baseFare) {
            this.baseFare = baseFare;
            return this;
        }

        public StdInputValues setBaseFare(double dollars) {
            this.baseFare = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public Money getXt() {
            return xt;
        }

        @Generated("by IDE")
        public StdInputValues setXt(Money xt) {
            this.xt = xt;
            return this;
        }

        public StdInputValues setXt(double dollars) {
            this.xt = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public BaseFareFromAirline getBaseFareFromAirline() {
            return baseFareFromAirline;
        }

        @Generated("by IDE")
        public StdInputValues setBaseFareFromAirline(BaseFareFromAirline baseFareFromAirline) {
            this.baseFareFromAirline = baseFareFromAirline;
            return this;
        }
    }

    static class ExpectedValues {
        private String name = "FLEX";
        private Money baseFare;
        private Money xt;
        private Money fareSalesTotal;
        private Money strikethru;
        private String ratePlanCode = "TFLX";

        @Generated("by IDE")
        public String getName() {
            return name;
        }

        @Generated("by IDE")
        public ExpectedValues setName(String name) {
            this.name = name;
            return this;
        }

        @Generated("by IDE")
        public Money getFareSalesTotal() {
            return fareSalesTotal;
        }

        @Generated("by IDE")
        public ExpectedValues setFareSalesTotal(Money fareSalesTotal) {
            this.fareSalesTotal = fareSalesTotal;
            return this;
        }
        public ExpectedValues setFareSalesTotal(double dollars) {
            this.fareSalesTotal = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public Money getStrikethru() {
            return strikethru;
        }

        @Generated("by IDE")
        public ExpectedValues setStrikethru(Money strikethru) {
            this.strikethru = strikethru;
            return this;
        }
        public ExpectedValues setStrikethru(double dollars) {
            this.strikethru = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public Money getBaseFare() {
            return baseFare;
        }

        @Generated("by IDE")
        public ExpectedValues setBaseFare(Money baseFare) {
            this.baseFare = baseFare;
            return this;
        }

        public ExpectedValues setBaseFare(double dollars) {
            this.baseFare = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public Money getXt() {
            return xt;
        }

        @Generated("by IDE")
        public ExpectedValues setXt(Money xt) {
            this.xt = xt;
            return this;
        }
        public ExpectedValues setXt(double dollars) {
            this.xt = Money.dollars(dollars);
            return this;
        }

        @Generated("by IDE")
        public String getRatePlanCode() {
            return ratePlanCode;
        }

        @Generated("by IDE")
        public ExpectedValues setRatePlanCode(final String ratePlanCode) {
            this.ratePlanCode = ratePlanCode;
            return this;
        }
    }


}
