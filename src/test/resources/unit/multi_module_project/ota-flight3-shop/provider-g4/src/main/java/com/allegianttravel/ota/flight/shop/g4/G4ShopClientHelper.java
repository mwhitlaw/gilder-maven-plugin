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

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiant.util.gen2.jaxrs.RestEasyClientBuilder;
import com.allegiantair.g4flights.service.client.dto.StatusDto;
import com.allegiantair.g4flights.service.client.response.ShopResponseDto;
import com.allegianttravel.ota.flight.shop.g4.client.G4ShopClient;
import com.allegianttravel.ota.flight.shop.provider.CsvQueryParam;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequestProviderInput;
import com.allegianttravel.ota.flight.shop.provider.ProviderApplicationException;
import com.allegianttravel.ota.flight.shop.rest.FareRuleType;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.allegianttravel.ota.flight.shop.g4.G4ShopCallUtil.execute;

/**
 * Helper class for invoking
 */
final class G4ShopClientHelper {

    private static final Logger logger = LoggerFactory.getLogger(G4ShopClientHelper.class);

    private G4ShopClientHelper() {}

    static G4ShopResponse invoke(FlightShopRequest shopRequest) {
        String endpoint = G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.makeEndpoint("/");

        logger.debug("g4 shopping for {} using {}", shopRequest, endpoint);

        ShopResponseDto shopResponseDto = invoke(shopRequest, endpoint);

        return new G4ShopResponse(shopResponseDto, shopRequest.getDepartDate());
    }

    static ShopResponseDto invoke(FlightShopRequest shopRequest, String endpoint) {
        final G4ShopClient g4Client = ServiceGenerator.create(G4ShopClient.class, endpoint);
        ShopResponseDto shopResponseDto;
        try {
            final com.allegiantair.g4flights.domain.BookingType bookingType =
                    com.allegiantair.g4flights.domain.BookingType.getBookingTypeByCode(shopRequest.getBookingType());
            final String couponCodes = CsvQueryParam.toCSV(shopRequest.getCouponCodes());
            Call<ShopResponseDto> call = g4Client.shopFlights(shopRequest.getDepartAirportCode(),
                    shopRequest.getArriveAirportCode(),
                    shopRequest.getDepartDate().minusDays(shopRequest.getReqMinusDays()),
                    shopRequest.getDepartDate().plusDays(shopRequest.getReqPlusDays()),
                    shopRequest.getChannelId(),
                    shopRequest.getPassengerCount(),
                    shopRequest.getBookingDate(),
                    bookingType.getBookingTypeCode(),
                    couponCodes,
                    shopRequest.getFareRuleType() == FareRuleType.ROUNDTRIP,
                    shopRequest.getRequestSourceId()
            );
            shopResponseDto = execute(call, ShopResponseDto.class, ShopResponseDto::new);
        } catch (Exception e) {
            shopResponseDto = new ShopResponseDto();
            shopResponseDto.setError(new StatusDto(e.getClass().getSimpleName(), e.getMessage()));
        }
        return shopResponseDto;
    }


    static Map<FlightRequest, FlightOptions> toOptionsByRequest(G4ShopResponse response,
                                                                FlightShopRequestProviderInput passengersRequest) {
        G4ShopDtoToFlightOptionMapper mapper = new G4ShopDtoToFlightOptionMapper(
                Collections.singletonList(response),
                passengersRequest.getPassengers());
        return mapper.groupByPairAndTravelDate();
    }

    /**
     * Converts the shop request into the G4 domain call details for the basic shop data. This is a simple mapping from
     * our domain into G4.
     * @param shopRequest
     * @return
     */
    static List<G4Callable> toShopRequests(FlightShopRequest shopRequest) {

        String endpoint = G4FlightProperties.G4_FLIGHTS_SHOP_ENDPOINT.getOrThrow();

        logger.debug("g4 shopping for {} using {}", shopRequest, endpoint);

        final G4ShopClient outboundClient = RestEasyClientBuilder.getProxy(G4ShopClient.class, endpoint);
        final com.allegiantair.g4flights.domain.BookingType bookingType =
                com.allegiantair.g4flights.domain.BookingType.valueOf(shopRequest.getBookingType());
        final String couponCodes = CsvQueryParam.toCSV(shopRequest.getCouponCodes());

        List<G4Callable> requests = new ArrayList<>();
        requests.add(new G4Callable(() -> outboundClient.shopFlights(shopRequest.getDepartAirportCode(),
                shopRequest.getArriveAirportCode(),
                shopRequest.getDepartDate().minusDays(shopRequest.getReqMinusDays()),
                shopRequest.getDepartDate().plusDays(shopRequest.getReqPlusDays()),
                shopRequest.getChannelId(),
                shopRequest.getPassengerCount(),
                shopRequest.getBookingDate(),
                bookingType.getBookingTypeCode(),
                couponCodes,
                shopRequest.getFareRuleType() == FareRuleType.ROUNDTRIP,
                shopRequest.getRequestSourceId()
        ), shopRequest.getDepartDate()));

        if (shopRequest.getReturnDate() != null) {
            final G4ShopClient inboundClient = RestEasyClientBuilder.getProxy(G4ShopClient.class, endpoint);
            requests.add(new G4Callable(() -> inboundClient.shopFlights(
                    shopRequest.getArriveAirportCode(),
                    shopRequest.getDepartAirportCode(),
                    shopRequest.getReturnDate().minusDays(shopRequest.getReqMinusDays()),
                    shopRequest.getReturnDate().plusDays(shopRequest.getReqPlusDays()),
                    shopRequest.getChannelId(),
                    shopRequest.getPassengerCount(),
                    shopRequest.getBookingDate(),
                    bookingType.getBookingTypeCode(),
                    couponCodes,
                    shopRequest.getFareRuleType() == FareRuleType.ROUNDTRIP,
                    shopRequest.getRequestSourceId()
            ), shopRequest.getReturnDate()));
        }

        return requests;
    }

    /**
     * Schedules all of the calls to the G4 services
     * @param g4ShopRequests
     * @return
     */
    static List<Future<G4ShopResponse>> fork(ExecutorService executorService, List<G4Callable> g4ShopRequests) {
        return g4ShopRequests.stream().map(executorService::submit).collect(Collectors.toList());
    }

    /**
     * Awaits the execution of all of the REST calls that are now available as Future objects
     * @param futures
     */
    static List<G4ShopResponse> join(List<Future<G4ShopResponse>> futures) throws ExecutionException, InterruptedException {

        List<G4ShopResponse> responses = new ArrayList<>();

        for (Future<G4ShopResponse> future : futures) {
            G4ShopResponse g4ShopResponse = future.get();
            if (g4ShopResponse.hasErrors()) {
                logErrors(g4ShopResponse);
                throw new ProviderApplicationException("Error calling shop service");
            } else {
                responses.add(g4ShopResponse);
            }
        }
        logger.debug("G4 returned {} responses", responses.size());
        return responses;
    }

    /**
     * Helper method to log errors
     * @param g4ShopResponse
     */
    private static void logErrors(G4ShopResponse g4ShopResponse) {
        if (logger.isErrorEnabled()) {
            g4ShopResponse.getErrorMessages()
                    .forEach(errorMessage -> logger.error("shop call error response code {}, message {}",
                            errorMessage.getCode(), errorMessage.getMessage()));
        }
    }

}
