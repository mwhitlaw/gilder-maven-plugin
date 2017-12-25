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

import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FlightShopRequestProviderInput {

    default int getPassengerCount() {
        Predicate<PassengerTypeCount> predicate = p -> true;
        return getPassengers()
                .stream()
                .filter(predicate)
                .mapToInt(PassengerTypeCount::getCount)
                .sum();
    }

    List<PassengerTypeCount> getPassengers();

    LocalDate getDepartDate();

    int getChannelId();

    String getBookingType();

    List<String> getCouponCodes();

    CallerMetadata getCallerMetadata();

    /**
     * Used to filter any additional fares. Used in a re-shopping scenario where someone is changing
     * their return flight or similar and is restricted to selecting a fare with the same name since
     * that's the fare that they're replacing. For example, changing your return flight when it's a
     * FLEX fare requires selecting the same FLEX fare for the new date of travel.
     * @return
     */
    Optional<String> getNamedFare();

    @SuppressWarnings("unused")
    default Stream<LocalDate> getDatesofTravel() {
        return Stream.of(getDepartDate());
    }
}
