/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.sabre;

import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AircraftCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Terminal;
import com.allegianttravel.ota.flight3.pricing.Fee;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.allegianttravel.ota.flight3.pricing.Tax;
import com.allegianttravel.sabre.generated.AirItineraryPricingInfo;
import com.allegianttravel.sabre.generated.Equipment;
import com.allegianttravel.sabre.generated.FareInfo_;
import com.allegianttravel.sabre.generated.FlightSegment;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRS;
import com.allegianttravel.sabre.generated.OnTimePerformance;
import com.allegianttravel.sabre.generated.OriginDestinationOption;
import com.allegianttravel.sabre.generated.PTCFareBreakdown;
import com.allegianttravel.sabre.generated.PricedItinerary;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.allegianttravel.ota.flight.shop.provider.ZoneIdUtils.toZoneId;

class SabreToFlightOptionMapper {

    private final OTAAirLowFareSearchRS response;
    private final Supplier<PackageId> packageIdSupplier;

    SabreToFlightOptionMapper(OTAAirLowFareSearchRS response,
                              Supplier<PackageId> packageIdSupplier) {
        this.response = response;
        this.packageIdSupplier = packageIdSupplier;
    }

    FlightOptions map() {
        FlightOptions options = new FlightOptions();
        return options.setFlightOptions(
                response.getPricedItineraries().getPricedItinerary().stream()
                        .map(this::toFlightOption)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Each OriginDestinationOption (ODO) within a PricedItinerary becomes a FlightOption in our domain. The ODO
     * models the flight(s) between an origin airport and a destination airport. If it's a non-stop flight, then this
     * object will have a single FlightSegment. If it's not a direct flight then the ODO will have multiple FlightSegment
     * objects.
     *
     * @param pricedItinerary
     * @return
     */
    private List<FlightOption> toFlightOption(PricedItinerary pricedItinerary) {
        List<FlightOption> options = new ArrayList<>();

        PackageId packageId = pricedItinerary.getAirItinerary().getOriginDestinationOptions()
                .getOriginDestinationOption().size() > 1 ? packageIdSupplier.get() : PackageId.NONE;

        for (OriginDestinationOption odo : pricedItinerary.getAirItinerary().getOriginDestinationOptions()
                .getOriginDestinationOption()) {
            FlightOption flightOption = new FlightOption();
            options.add(flightOption);

            ProviderFares providerFaresOption = new ProviderFares()
                    .setPackageId(packageId)
                    .setFareClassCodes(odo.getFlightSegment().stream()
                            .map(FlightSegment::getResBookDesigCode)
                            .collect(Collectors.toList()))
                    .setProviderId("sabre");

            flightOption.setProviderFares(Collections.singletonList(providerFaresOption));

            flightOption.setSegments(odo.getFlightSegment()
                    .stream()
                    .map(this::toSegment)
                    .collect(Collectors.toList())
            );
        }


        // set the pricing infos on the outbound flight, all the rest will be $0 (
        FlightOption firstOption = options.get(0);
        List<FareDetails> fareDetails = new ArrayList<>();
        List<PassengerTypeCount> ptcList = new ArrayList<>();
        for (AirItineraryPricingInfo airItineraryPricingInfo : pricedItinerary.getAirItineraryPricingInfo()) {
            List<FareInfo_> fareInfos = airItineraryPricingInfo.getFareInfos().getFareInfo();
            PTCFareBreakdown ptcFareBreakdown = airItineraryPricingInfo.getPTCFareBreakdowns()
                    .getPTCFareBreakdown().get(0);
            List<PassengerTypeCount> passengerTypeCounts = toPassengerTypeCount(ptcFareBreakdown);
            ptcList.addAll(passengerTypeCounts);
            FareDetails pricing = new FareDetails()
                    .setApplicablePassengerTypes(passengerTypeCounts
                            .stream()
                            .map(PassengerTypeCount::getPassengerType)
                            .collect(Collectors.toList()))
                    .setAvailableSeats(
                            fareInfos.stream()
                                    .filter(fi -> fi.getTPAExtensions() != null)
                                    .filter(fi -> fi.getTPAExtensions().getSeatsRemaining() != null)
                                    .map(fi -> fi.getTPAExtensions().getSeatsRemaining().getNumber())
                                    .findFirst()
                                    .orElse(null))
                    .setBaseFare(Money.amount(ptcFareBreakdown.getPassengerFare().getBaseFare().getAmount(),
                            ptcFareBreakdown.getPassengerFare().getBaseFare().getCurrencyCode()))
                    .setTaxes(ptcFareBreakdown.getPassengerFare().getTaxes().getTax()
                            .stream()
                            .map(t -> new Tax().setCode(t.getTaxCode())
                                    .setAmount(Money.amount(t.getAmount(), t.getCurrencyCode())))
                            .collect(Collectors.toList()));
            // sabre-demo - sabre fares don't show fees. Likely a data issue with the test platform.
            if (ptcFareBreakdown.getPassengerFare().getFees() != null) {
                pricing.setFees(ptcFareBreakdown.getPassengerFare().getFees().getFee()
                        .stream()
                        .map(f -> new Fee().setCode(f.getFeeCode())
                                .setAmount(Money.amount(f.getAmount(), f.getCurrencyCode())))
                        .collect(Collectors.toList()));
            }
            fareDetails.add(pricing);
        }
        firstOption.getProviderFares().get(0).addNamedFare(new NamedFare(ProviderFares.STD_RATE, fareDetails, ptcList));

        return options;
    }

    private List<PassengerTypeCount> toPassengerTypeCount(PTCFareBreakdown ptcFareBreakdown) {
        return Collections.singletonList(SabreUtils.fromPassengerTypeQuantity(
                ptcFareBreakdown.getPassengerTypeQuantity()));
    }

    private Segment toSegment(FlightSegment fs) {
        Equipment equipment = fs.getEquipment().get(0);
        ZoneId departureZoneId = toZoneId(fs.getDepartureTimeZone().getGMTOffset());
        ZoneId arrivalZoneId = toZoneId(fs.getArrivalTimeZone().getGMTOffset());
        return new Segment()
                .setFlightNumber(new FlightNumber(fs.getFlightNumber()))
                .setOperatorCode(new AirlineCode(fs.getOperatingAirline().getCode()))
                .setCarrierCode(new AirlineCode(fs.getMarketingAirline().getCode()))
                .setLegs(Collections.singletonList(
                        new Leg()
                                .setOrigin(new AirportCode(fs.getDepartureAirport().getLocationCode()))
                                .setDestination(new AirportCode(fs.getArrivalAirport().getLocationCode()))
                                .setOriginTerminal(Terminal.of(fs.getDepartureAirport().getTerminalID()))
                                .setDestinationTerminal(Terminal.of(fs.getArrivalAirport().getTerminalID()))
                                .setAircraftCode(AircraftCode.of(equipment.getAirEquipType()))
                                .setArrivalTime(fs.getArrivalDateTime())
                                .setDepartureTime(fs.getDepartureDateTime())
                                .setGmtDepartureTime(fs.getDepartureDateTime()
                                        .atZone(departureZoneId).toInstant().atZone(ZoneId.of("GMT")).toLocalDateTime())
                                .setGmtArrivalTime(fs.getArrivalDateTime().atZone(arrivalZoneId)
                                        .toInstant().atZone(ZoneId.of("GMT")).toLocalDateTime())
                                .setDuration(fs.getElapsedTime())
                                // sabre-demo need connection info
                                .setConnectionInfo(null)
                                .setOnTimePerformance(getOnTimePerformance(fs))
                ));
    }

    private Integer getOnTimePerformance(FlightSegment fs) {
        Integer performance;
        OnTimePerformance onTimePerformance = fs.getOnTimePerformance();
        if (onTimePerformance != null) {
            try {
                performance = Integer.valueOf(onTimePerformance.getLevel());
            } catch (NumberFormatException e) {
                // this is modeled as a string in the schema. I've seen it come back as "N" in some cases
                performance = null;
            }
        } else {
            performance = null;
        }
        return performance;
    }
}
