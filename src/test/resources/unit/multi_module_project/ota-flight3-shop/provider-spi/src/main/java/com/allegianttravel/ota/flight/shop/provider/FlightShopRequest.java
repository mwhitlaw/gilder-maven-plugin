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

import com.allegianttravel.ota.flight.shop.rest.FareRuleType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlexibleTravelRequest;
import com.allegianttravel.ota.flight3.util.DaysOfTravel;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Generated;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FlightShopRequest extends AbstractFlightShopRequestProviderInput
        implements FlexibleTravelRequest, FlightShopRequestProviderInput {
    private final AirportCode departAirportCode;
    private final AirportCode arriveAirportCode;
    private final LocalDate departDate;
    private final LocalDate returnDate;
    private final int channelId;
    private final List<PassengerTypeCount> passengers;
    private final String bookingType;
    private final int reqPlusDays;
    private final int reqMinusDays;
    private final LocalDate bookingDate;
    private final List<String> couponCodes;
    private final CallerMetadata callerMetadata;
    private final int maxResults;
    private final String namedFare;
    private final FareRuleType fareRuleType;
    private final Integer requestSourceId;

    public FlightShopRequest(Builder builder) {
        this.departAirportCode = builder.getDepartAirportCode();
        this.arriveAirportCode = builder.getArriveAirportCode();
        this.departDate = builder.getDepartDate();
        this.returnDate = builder.getReturnDate();
        this.channelId = builder.getChannelId();
        this.passengers = Collections.unmodifiableList(builder.getPassengers());
        this.bookingType = builder.getBookingType();
        this.reqPlusDays = builder.getReqPlusDays();
        this.reqMinusDays = builder.getReqMinusDays();
        this.bookingDate = builder.getBookingDate() == null ? LocalDate.now() : builder.getBookingDate();
        this.couponCodes = builder.getCouponCodes() == null ? Collections.emptyList() :
                Collections.unmodifiableList(builder.getCouponCodes());
        this.callerMetadata = builder.getCallerMetadata();
        this.maxResults = builder.getMaxResults();
        this.namedFare = StringUtils.isNotEmpty(builder.getNamedFare()) ? builder.getNamedFare() : null;
        this.fareRuleType = builder.getFareRuleType();
        this.requestSourceId = builder.getRequestSourceId();
    }

    @Override
    public Stream<LocalDate> getDatesofTravel() {
        return DaysOfTravel.toDaysOfTravel(
                getReqPlusDays(),
                getReqMinusDays(),
                getDepartDate(),
                getReturnDate());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(FlightShopRequest request) {
        return new Builder()
                .setDepartAirportCode(request.getDepartAirportCode())
                .setArriveAirportCode(request.getArriveAirportCode())
                .setDepartDate(request.getDepartDate())
                .setReturnDate(request.getReturnDate())
                .setChannelId(request.getChannelId())
                .setPassengers(request.getPassengers())
                .setBookingType(request.getBookingType())
                .setReqPlusDays(request.getReqPlusDays())
                .setReqMinusDays(request.getReqMinusDays())
                .setBookingDate(request.getBookingDate())
                .setCouponCodes(request.getCouponCodes())
                .setCallerMetadata(request.getCallerMetadata())
                .setMaxResults(request.getMaxResults())
                .setNamedFare(request.getNamedFare().orElse(null));
    }

    public static class Builder {
        private AirportCode departAirportCode;
        private AirportCode arriveAirportCode;
        private LocalDate departDate;
        private LocalDate returnDate;
        private int channelId;
        private List<PassengerTypeCount> passengers = Collections.emptyList();
        private String bookingType;
        private int reqPlusDays;
        private int reqMinusDays;
        private LocalDate bookingDate;
        private List<String> couponCodes;
        private CallerMetadata callerMetadata;
        private int maxResults = 20;
        private String namedFare;
        private FareRuleType fareRuleType;
        private Integer requestSourceId;

        public FlightShopRequest build() {
            return new FlightShopRequest(this);
        }

        @Generated("by IDE")
        public LocalDate getDepartDate() {
            return departDate;
        }

        @Generated("by IDE")
        public Builder setDepartDate(LocalDate departDate) {
            this.departDate = departDate;
            return this;
        }

        @Generated("by IDE")
        public LocalDate getReturnDate() {
            return returnDate;
        }

        @Generated("by IDE")
        public Builder setReturnDate(LocalDate returnDate) {
            this.returnDate = returnDate;
            return this;
        }

        @Generated("by IDE")
        public AirportCode getDepartAirportCode() {
            return departAirportCode;
        }

        @Generated("by IDE")
        public Builder setDepartAirportCode(AirportCode departAirportCode) {
            this.departAirportCode = departAirportCode;
            return this;
        }

        @Generated("by IDE")
        public AirportCode getArriveAirportCode() {
            return arriveAirportCode;
        }

        @Generated("by IDE")
        public Builder setArriveAirportCode(AirportCode arriveAirportCode) {
            this.arriveAirportCode = arriveAirportCode;
            return this;
        }

        @Generated("by IDE")
        public int getChannelId() {
            return channelId;
        }

        @Generated("by IDE")
        public Builder setChannelId(int channelId) {
            this.channelId = channelId;
            return this;
        }

        @Generated("by IDE")
        public List<PassengerTypeCount> getPassengers() {
            return passengers;
        }

        @Generated("by IDE")
        public Builder setPassengers(List<PassengerTypeCount> passengers) {
            this.passengers = passengers;
            return this;
        }

        @Generated("by IDE")
        public String getBookingType() {
            return bookingType;
        }

        @Generated("by IDE")
        public Builder setBookingType(String bookingType) {
            this.bookingType = bookingType;
            return this;
        }

        @Generated("by IDE")
        public int getReqPlusDays() {
            return reqPlusDays;
        }

        @Generated("by IDE")
        public Builder setReqPlusDays(int reqPlusDays) {
            this.reqPlusDays = reqPlusDays;
            return this;
        }

        @Generated("by IDE")
        public int getReqMinusDays() {
            return reqMinusDays;
        }

        @Generated("by IDE")
        public Builder setReqMinusDays(int reqMinusDays) {
            this.reqMinusDays = reqMinusDays;
            return this;
        }

        @Generated("by IDE")
        public LocalDate getBookingDate() {
            return bookingDate;
        }

        @Generated("by IDE")
        public Builder setBookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        @Generated("by IDE")
        public List<String> getCouponCodes() {
            return couponCodes;
        }

        @Generated("by IDE")
        public Builder setCouponCodes(List<String> couponCodes) {
            this.couponCodes = couponCodes;
            return this;
        }

        @Generated("by IDE")
        public CallerMetadata getCallerMetadata() {
            return callerMetadata;
        }

        @Generated("by IDE")
        public Builder setCallerMetadata(CallerMetadata callerMetadata) {
            this.callerMetadata = callerMetadata;
            return this;
        }

        @Generated("by IDE")
        public int getMaxResults() {
            return maxResults;
        }

        @Generated("by IDE")
        public Builder setMaxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        @Generated("by IDE")
        public String getNamedFare() {
            return namedFare;
        }

        @Generated("by IDE")
        public Builder setNamedFare(String namedFare) {
            this.namedFare = namedFare;
            return this;
        }

        @Generated("by IDE")
        public FareRuleType getFareRuleType() {
            return fareRuleType;
        }

        @Generated("by IDE")
        public Builder setFareRuleType(FareRuleType fareRuleType) {
            this.fareRuleType = fareRuleType;
            return this;
        }

        @Generated("by IDE")
        public Integer getRequestSourceId() {
            return requestSourceId;
        }

        @Generated("by IDE")
        public Builder setRequestSourceId(Integer requestSourceId) {
            this.requestSourceId = requestSourceId;
            return this;
        }
    }

    @Generated("by IDE")
    public AirportCode getDepartAirportCode() {
        return departAirportCode;
    }

    @Generated("by IDE")
    public AirportCode getArriveAirportCode() {
        return arriveAirportCode;
    }

    @Generated("by IDE")
    public LocalDate getDepartDate() {
        return departDate;
    }

    @Generated("by IDE")
    public LocalDate getReturnDate() {
        return returnDate;
    }

    @Generated("by IDE")
    public int getChannelId() {
        return channelId;
    }

    @Override
    @Generated("by IDE")
    public List<PassengerTypeCount> getPassengers() {
        return passengers;
    }

    @Generated("by IDE")
    public String getBookingType() {
        return bookingType;
    }

    @Override
    @Generated("by IDE")
    public int getReqPlusDays() {
        return reqPlusDays;
    }

    @Override
    @Generated("by IDE")
    public int getReqMinusDays() {
        return reqMinusDays;
    }

    @Generated("by IDE")
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    @Generated("by IDE")
    public List<String> getCouponCodes() {
        return couponCodes;
    }

    @Generated("by IDE")
    public CallerMetadata getCallerMetadata() {
        return callerMetadata;
    }

    @Generated("by IDE")
    public int getMaxResults() {
        return maxResults;
    }

    public Optional<String> getNamedFare() {
        return Optional.ofNullable(namedFare);
    }

    @Generated("by IDE")
    public FareRuleType getFareRuleType() {
        return fareRuleType;
    }

    @Generated("by IDE")
    public Integer getRequestSourceId() {
        return requestSourceId;
    }

    @Override
    @Generated("by IDE")
    public String toString() {
        return "FlightShopRequest{" +
                "departAirportCode=" + departAirportCode +
                ", arriveAirportCode=" + arriveAirportCode +
                ", departDate=" + departDate +
                ", returnDate=" + returnDate +
                ", channelId=" + channelId +
                ", passengers=" + passengers +
                ", bookingType='" + bookingType + '\'' +
                ", reqPlusDays=" + reqPlusDays +
                ", reqMinusDays=" + reqMinusDays +
                ", bookingDate=" + bookingDate +
                ", couponCodes=" + couponCodes +
                ", callerMetadata=" + callerMetadata +
                ", maxResults=" + maxResults +
                ", namedFare='" + namedFare + '\'' +
                ", requestSourceId='" + requestSourceId + '\'' +
                ", fareRuleType=" + fareRuleType +
                '}';
    }
}
