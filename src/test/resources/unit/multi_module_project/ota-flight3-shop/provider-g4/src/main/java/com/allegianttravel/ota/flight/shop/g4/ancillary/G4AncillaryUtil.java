/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.ancillary;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiant.util.property.MissingPropertyException;
import com.allegiantair.g4flights.ancillary.service.rest.AncillaryFeesByDateResponse;
import com.allegiantair.g4flights.domain.FeeType;
import com.allegiantair.g4flights.service.client.dto.AncillaryFeeDto;
import com.allegiantair.g4flights.service.client.dto.PropertyDto;
import com.allegianttravel.ota.flight.shop.g4.G4FlightProperties;
import com.allegianttravel.ota.flight.shop.g4.client.G4AncillaryFeesClient;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.Severity;
import com.allegianttravel.ota.flight.shop.rest.dto.ancillary.AncillaryFee;
import com.allegianttravel.ota.flight.shop.rest.dto.ancillary.AncillaryFees;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightFees;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import com.allegianttravel.ota.flight3.pricing.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

public final class G4AncillaryUtil {

    private static final Logger logger = LoggerFactory.getLogger(G4AncillaryUtil.class);

    private G4AncillaryUtil() {

    }

    /**
     * Gets the fees for all of the days of travel in the shop request
     *
     * @param shopRequest where and when the customer wants to fly
     * @throws AncillaryException
     */
    public static FlightFees getFlightFeesForDaysOfTravel(FlightShopRequest shopRequest) throws AncillaryException {
        AncillaryFeesByDateResponse fromG4Ancillary = getFeesForDaysOfTravel(shopRequest);
        return mapFromG4Ancillary(fromG4Ancillary);
    }

    /**
     * Performs the call and returns the g4 payload. We'll map this to our own payload separately
     *
     * @param shopRequest
     * @return
     * @throws AncillaryException
     */
    private static AncillaryFeesByDateResponse getFeesForDaysOfTravel(FlightShopRequest shopRequest)
            throws AncillaryException {

        String endpoint;
        try {
            endpoint = G4FlightProperties.G4_FLIGHTS_ANCILLARY_SERVICE_ENDPOINT.makeEndpoint("/");
        } catch (MissingPropertyException e) {
            throw new AncillaryException(Severity.WARNING, "Failed to inoke ancillary service", "missing endpoint", e);
        }

        G4AncillaryFeesClient client = ServiceGenerator.create(G4AncillaryFeesClient.class, endpoint);
        Call<AncillaryFeesByDateResponse> call = client.getFees(
                shopRequest.getDepartAirportCode(),
                shopRequest.getArriveAirportCode(),
                shopRequest.getDepartDate(),
                shopRequest.getChannelId(),
                shopRequest.getBookingDate(),
                shopRequest.getReqPlusDays(),
                shopRequest.getReqMinusDays(),
                shopRequest.getReturnDate());
        try {
            Response<AncillaryFeesByDateResponse> response = call.execute();
            if (response.isSuccessful()) {
                logger.debug("got fees result for market {} to {}", shopRequest.getDepartAirportCode(),
                        shopRequest.getArriveAirportCode());
                return response.body();
            } else {
                logger.warn("Error response ancillary service {}. Code: {} Payload: {}",
                        call.request().url(), response.code(), response.errorBody().string());
                throw new AncillaryException(Severity.WARNING, "Error calling ancillary service",
                        call.request().url().toString(),
                        response.code(), response.errorBody().string(), null);
            }
        } catch (IOException e) {
            logger.warn("Error calling ancillary service {}, no response from service", call.request().url());
            throw new AncillaryException(Severity.WARNING, "Error calling ancillary service",
                    call.request().url().toString(), e);
        }
    }

    /**
     * Maps the whole fee response over to our domain
     *
     * @param feesByDateResponse
     * @return
     */
    static FlightFees mapFromG4Ancillary(AncillaryFeesByDateResponse feesByDateResponse) {
        FlightFees flightFees = new FlightFees();

        flightFees.setUniversalFees(feesByDateResponse.getSsrFees()
                .stream()
                .map(G4AncillaryUtil::mapFeeDto)
                .collect(Collectors.toList())
        );

        flightFees.setFlightSpecificFees(feesByDateResponse.getFlightOptionFees().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> new FlightRequest(
                                AirportCode.of(entry.getKey().getOrigin()),
                                AirportCode.of(entry.getKey().getDestination()),
                                entry.getKey().getTravelDate()),
                        entry -> new AncillaryFees()
                                .setFeeList(entry.getValue()
                                        .stream()
                                        .map(G4AncillaryUtil::mapFeeDto)
                                        .collect(Collectors.toList())
                                )
                )));

        return flightFees;
    }

    /**
     * Maps a fee over from the g4 domain to our domain
     *
     * @param feeDto
     * @return
     */
    private static AncillaryFee mapFeeDto(AncillaryFeeDto feeDto) {
        AncillaryFee.Type type = toType(feeDto);
        AncillaryFee retVal;
        if (type != null) {
            retVal = new AncillaryFee()
                    .setCode(translateCode(feeDto, type))
                    .setProperties(feeDto.getProperties() == null ?
                            Collections.emptyMap() :
                            feeDto.getProperties()
                                    .stream()
                                    // skip over any cumulative
                                    .filter(propertyDto -> !"CUMULATIVE".equals(propertyDto.getName()))
                                    .collect(Collectors.toMap(PropertyDto::getName, PropertyDto::getValue)))
                    .setCharge(Money.dollars(feeDto.getValue()))
                    .setDescription(feeDto.getDescription())
                    .setType(type);
            if (type == AncillaryFee.Type.BAG_FEE) {
                Money charge = retVal.getCharge();
                String property = feeDto.getProperty("CUMULATIVE");
                if (property != null) {
                    retVal.setCharge(Money.dollars(new BigDecimal(property)));
                }
                retVal.getProperties().put("AddChg", charge.getAmount() + "");
            }
        } else {
            retVal = null;
        }
        return retVal;
    }

    private static String translateCode(final AncillaryFeeDto feeDto, AncillaryFee.Type type) {
        String code = feeDto.getCode();
        if (type == AncillaryFee.Type.BAG_FEE) {
            String legacyBagCode;
            // see FLX-76
            if ("AC".equals(code)) {
                legacyBagCode = "ABB";
            } else {
                legacyBagCode = "B" + code;
            }
            return legacyBagCode;
        } else {
            return code;
        }
    }


    /**
     * Maps the g4 fee type over to our domain
     *
     * @param feeDto
     * @return
     */
    private static AncillaryFee.Type toType(AncillaryFeeDto feeDto) {
        if (feeDto.getType() == FeeType.BAG_FEE.getId()) {
            return AncillaryFee.Type.BAG_FEE;
        } else if (feeDto.getType() == FeeType.SSR.getId()) {
            return AncillaryFee.Type.SSR;
        } else if (feeDto.getType() == FeeType.PRIORITY_BOARDING.getId()) {
            return AncillaryFee.Type.PRIORITY_BOARDING;
        } else {
            return null;
        }
    }
}
