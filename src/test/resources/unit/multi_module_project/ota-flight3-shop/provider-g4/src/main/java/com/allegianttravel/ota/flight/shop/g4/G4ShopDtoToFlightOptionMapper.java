/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4;

import com.allegiantair.g4flights.service.client.dto.DiscountReasonDto;
import com.allegiantair.g4flights.service.client.dto.EquipmentDto;
import com.allegiantair.g4flights.service.client.dto.FeeDto;
import com.allegiantair.g4flights.service.client.dto.SegmentDto;
import com.allegiantair.g4flights.service.client.dto.ShopDto;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.price.BaseFareFromAirline;
import com.allegianttravel.ota.flight.shop.rest.dto.price.DiscountReason;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AircraftCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.flight3.pricing.Fee;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.allegianttravel.ota.flight3.pricing.Tax;
import com.allegianttravel.ota.flight3.pricing.TaxCalculation;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps the G4 response into our domain
 */
class G4ShopDtoToFlightOptionMapper {

    private final List<PassengerTypeCount> passengerTypes;
    private final List<G4ShopResponse> responses;

    G4ShopDtoToFlightOptionMapper(List<G4ShopResponse> responses, List<PassengerTypeCount> passengerTypes) {
        this.responses = responses;
        this.passengerTypes = passengerTypes;
    }

    Map<FlightRequest, FlightOptions> groupByPairAndTravelDate() {

        Map<FlightRequest, FlightOptions> optionsByRequest = new LinkedHashMap<>();

        for (G4ShopResponse g4ShopResponse : responses) {
            LocalDate travelDate = g4ShopResponse.getTravelDate();

            List<FlightOption> options = g4ShopResponse.getShopDtos()
                    .stream()
                    .map(this::map)
                    .collect(Collectors.toList());

            if (!options.isEmpty()) {
                // Don't want to assume that all of the flight options for G4 consist of single segment / single
                // leg flights.
                // This may be true today but it could change in the future.
                // However, it should be safe to assume that all of the flight options within a single g4Reponse are for
                // the same trip and thus the same origin/destination.
                AirportCode firstOrigin = options.get(0).getFirstOrigin();
                AirportCode finalDestination = options.get(0).getFinalDestination();

                FlightRequest key = new FlightRequest(firstOrigin, finalDestination, travelDate);
                FlightOptions previous = optionsByRequest.put(key, new FlightOptions().setFlightOptions(options));
                assert previous == null;
            }
        }

        return optionsByRequest;
    }


    private FlightOption map(ShopDto shopDto) {
        FlightOption option = new FlightOption();
        Segment segment = new Segment();
        SegmentDto g4Segment = shopDto.getSegment();
        String airlineCode = g4Segment.getAirlineCode();
        AircraftCode aircraftCode = toAircraftCode(g4Segment.getEquipment());
        segment.setCarrierCode(AirlineCode.of(airlineCode));
        segment.setFlightNumber(new FlightNumber(g4Segment.getFltNum()));
        segment.setLegs(g4Segment.getLegs()
                .stream()
                .map(
                        g4Leg -> new Leg()
                                .setOrigin(new AirportCode(g4Leg.getDepartAirport()))
                                .setDestination(new AirportCode(g4Leg.getArriveAirport()))
                                .setArrivalTime(LocalDateTime.parse(g4Leg.getScheduledArriveDateTime()))
                                .setDepartureTime(LocalDateTime.parse(g4Leg.getScheduledDepartDateTime()))
                                .setGmtDepartureTime(LocalDateTime.parse(g4Leg.getGmtScheduledDepartDateTime()))
                                .setGmtArrivalTime(LocalDateTime.parse(g4Leg.getGmtScheduledArriveDateTime()))
                                .setDuration(minutesBetween(
                                        LocalDateTime.parse(g4Leg.getGmtScheduledDepartDateTime())
                                                .toInstant(ZoneOffset.UTC),
                                        LocalDateTime.parse(g4Leg.getGmtScheduledArriveDateTime())
                                                .toInstant(ZoneOffset.UTC)))
                                .setMiles(g4Leg.getRouteMiles())
                                .setAircraftCode(aircraftCode)
                                .setAircraftMake(g4Segment.getEquipment().getMake())
                                .setAircraftModel(g4Segment.getEquipment().getModel())
                )
                .collect(Collectors.toList())
        );

        FareDetails fareDetails = toFareDetails(shopDto);
        option.setProviderFares(Collections.singletonList(
                new ProviderFares()
                        .setProviderId("g4")
                        // there's only a single segment so no list here
                        .setFareClassCodes(Collections.singletonList(
                                shopDto.getFareAndAvail().getFare().getFareClassCode()))
                        .addNamedFare(
                                new NamedFare(ProviderFares.STD_RATE,
                                        Collections.singletonList(fareDetails), passengerTypes)
                                .setRatePlanCodes(shopDto.getFareAndAvail().getFare().getRatePlanCode() != null ?
                                        Collections.singletonList(
                                                shopDto.getFareAndAvail().getFare().getRatePlanCode()) : null)
                        )
                        )
        );
        option.setSegments(Collections.singletonList(segment));
        return option;
    }

    private FareDetails toFareDetails(ShopDto shopDto) {
        Set<DiscountReasonDto> discountReasons = shopDto.getFareAndAvail().getFare().getDiscountReasons();
        return new FareDetails()
                .setApplicablePassengerTypes(
                        passengerTypes
                                .stream()
                        .map(PassengerTypeCount::getPassengerType)
                        .collect(Collectors.toList()))
                .setBaseFare(Money.dollars(shopDto.getFareAndAvail().getFare().getBaseFare()))
                .setAvailableSeats(shopDto.getFareAndAvail().getFareClassInventory().getNumberAvail())
                .setCouponCode(shopDto.getFareAndAvail().getFare().getCouponCode())
                .setBaseFareFromAirline(
                        discountReasons != null && !discountReasons.isEmpty() ?
                        new BaseFareFromAirline()
                                .setStrikethruBaseFare(Money.dollars(
                                        shopDto.getFareAndAvail().getFare().getStrikeThruBaseFare()))
                                .setStrikethruTax(Money.dollars(
                                        shopDto.getFareAndAvail().getFare().getStrikeThruExciseTax()))
                                .setDiscountsApplied(
                                        discountReasons
                                                .stream()
                                                .map(dto -> new DiscountReason()
                                                        .setName(dto.getName())
                                                        .setPostTaxAmount(Money.dollars(dto.getPostTaxAmount()))
                                                        .setAmount(Money.dollars(dto.getAmount())))
                                                .sorted()
                                                .collect(Collectors.toList())) : null)
                .setFees(
                        shopDto.getFareAndAvail().getFare().getFee()
                                .stream()
                                .filter(f -> f.isPercentage() == null || !f.isPercentage())
                                .map(f ->
                                        new Fee()
                                                .setDescription(f.getDescription())
                                                .setAmount(Money.dollars(f.getValue()))
                                                .setCode(f.getCode())
                                )
                                .collect(Collectors.toList())
                )
                .setTaxes(
                        shopDto.getFareAndAvail().getFare().getFee()
                                .stream()
                                .filter(f -> f.isPercentage() != null)
                                .filter(FeeDto::isPercentage)
                                .map(f ->
                                        new Tax()
                                                .setDescription(f.getDescription())
                                                .setAmount(Money.dollars(f.getValue()))
                                                .setCode(f.getCode())
                                                .setCalculation(toCalculation(f))
                                )
                                .collect(Collectors.toList())
                );
    }

    private TaxCalculation toCalculation(FeeDto f) {
        return new TaxCalculation()
                .setAmount(f.getRate())
                .setName(f.getDescription())
                .setType(f.isPercentage() ? TaxCalculation.Type.PERCENTAGE : TaxCalculation.Type.FIXED);
    }

    private AircraftCode toAircraftCode(EquipmentDto equipment) {
        AircraftCode code = null;
        if (equipment != null) {
//            String make = StringUtils.defaultString(equipment.getMake());
            String model = StringUtils.defaultString(equipment.getModel());
            code = AircraftCode.of(model);
        }
        return code;
    }


    private static int minutesBetween(Instant t1, Instant t2) {
        return (int) Math.abs(ChronoUnit.MINUTES.between(t1, t2));
    }

}
