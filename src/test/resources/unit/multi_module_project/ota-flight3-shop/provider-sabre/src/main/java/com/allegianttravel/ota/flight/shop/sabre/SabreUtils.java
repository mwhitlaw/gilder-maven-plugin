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

import com.allegiant.sabre.SabreCredentials;
import com.allegiant.sabre.TokenAuthClient;
import com.allegiant.sabre.TokenAuthException;
import com.allegiant.sabre.TokenAuthResponse;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.price.FareDetails;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderFares;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlexibleTravelRequest;
import com.allegianttravel.ota.flight3.pricing.Money;
import com.allegianttravel.sabre.generated.AirTravelerAvail;
import com.allegianttravel.sabre.generated.CompanyName;
import com.allegianttravel.sabre.generated.DateFlexibility;
import com.allegianttravel.sabre.generated.DestinationLocation;
import com.allegianttravel.sabre.generated.IntelliSellTransaction;
import com.allegianttravel.sabre.generated.NumTrips;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQ;
import com.allegianttravel.sabre.generated.OriginDestinationInformation;
import com.allegianttravel.sabre.generated.OriginLocation;
import com.allegianttravel.sabre.generated.POS;
import com.allegianttravel.sabre.generated.PassengerTypeQuantity;
import com.allegianttravel.sabre.generated.PassengerTypeQuantity_;
import com.allegianttravel.sabre.generated.RequestType;
import com.allegianttravel.sabre.generated.RequestorID;
import com.allegianttravel.sabre.generated.Source;
import com.allegianttravel.sabre.generated.TPAExtensions;
import com.allegianttravel.sabre.generated.TPAExtensions_;
import com.allegianttravel.sabre.generated.TPAExtensions_____;
import com.allegianttravel.sabre.generated.TravelPreferences;
import com.allegianttravel.sabre.generated.TravelerInfoSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utils for interacting with Sabre
 */
public final class SabreUtils {

    static final AuthTokenFunctions AUTH_TOKEN_FUNCTIONS = () -> SabreUtils.setAuthToken(null);

    private static final Logger logger = LoggerFactory.getLogger(SabreUtils.class);

    /**
     * The token issued from the Sabre Auth Token Service. We'll set this during the start of the application.
     */
    private static String token = null;

    private SabreUtils() {}

    static OTAAirLowFareSearchRQ createRoundtripRequest(FlightShopRequest shopRequest) throws IOException {

        assert shopRequest.getReturnDate() != null;

        // convert the shop request to Sabre's list of origin/destinations
        List<OriginDestinationInformation> odoList = new ArrayList<>();

        // This is the outbound flight. If it's a one-way, another entry will be added below
        OriginDestinationInformation outbound = new OriginDestinationInformation()
                .withDepartureDateTime(shopRequest.getDepartDate().atStartOfDay())
                .withOriginLocation(new OriginLocation()
                        .withLocationCode(shopRequest.getDepartAirportCode().getValue()))
                .withDestinationLocation(new DestinationLocation().withLocationCode(
                        shopRequest.getArriveAirportCode().getValue()));

        // The TPAExtensions handle the date flexibility. This allows us to search for +/- days on our travel date
        TPAExtensions tpaExtensions = buildFlexibilityExtensions(shopRequest);
        outbound.setTPAExtensions(tpaExtensions);
        odoList.add(outbound);


        // add the return date
        OriginDestinationInformation inbound = new OriginDestinationInformation()
                .withDepartureDateTime(shopRequest.getReturnDate().atStartOfDay())
                .withOriginLocation(new OriginLocation()
                        .withLocationCode(shopRequest.getArriveAirportCode().getValue()))
                .withDestinationLocation(new DestinationLocation().withLocationCode(
                        shopRequest.getDepartAirportCode().getValue()));

        inbound.setTPAExtensions(tpaExtensions);

        odoList.add(inbound);
        TravelPreferences travelPreferences = createTravelerPreferences(shopRequest, shopRequest.getMaxResults());

        // Sabre asks for the passengers in their shop api. It's not clear if this affects the prices beyond a simple
        // head count.
        TravelerInfoSummary travelerInfoSummary = toTravelerInfoSummary(shopRequest.getPassengers());

        // The top level search request with all of the info assembled above
        return new OTAAirLowFareSearchRQ()
                .withOriginDestinationInformation(odoList)
                .withPOS(createPOS())
                .withTPAExtensions(createIntelliSellTx())
                .withTravelPreferences(travelPreferences)
                .withTravelerInfoSummary(travelerInfoSummary);
    }

    static String getAuthToken() {
        if (token == null) {
            try {
                refreshToken();
            } catch (TokenAuthException | MalformedURLException e) {
                logger.error("Error getting sabre auth token", e);
            }
        }
        return token;
    }

    static void setAuthToken(String authToken) {
        token = authToken;
    }


    private static String toSabrePassengerCode(PassengerTypeCount passenger) {
        // https://richmedia.sabre.com/docs_support/quickreferences/gen/price54.pdf
        switch (passenger.getPassengerType()) {
            case ADULT:
                return "ADT";
            case CHILD:
                // the sabre code is C + zero padded age
                return "C07";
            case INFANT:
                return "INS";
            case INFANT_ON_LAP:
                return "INF";
            case SENIOR:
                // the sabre code is B + age
                return "B68";
            default:
                return "ADT";
        }
    }

    static PassengerTypeCount fromPassengerTypeQuantity(PassengerTypeQuantity_ ptq) {
        if (ptq == null) {
            return new PassengerTypeCount(PassengerType.ADULT, 1);
        }
        PassengerType pt;
        if (ptq.getCode() == null || "ADT".equals(ptq.getCode())) {
            pt = PassengerType.ADULT;
        } else if ("C".equals(ptq.getCode())) {
            pt = PassengerType.CHILD;
        } else if ("INS".equals(ptq.getCode())) {
            pt = PassengerType.INFANT;
        } else if (ptq.getCode().startsWith("B")) {
            pt = PassengerType.SENIOR;
        } else {
            pt = PassengerType.ADULT;
        }
        return new PassengerTypeCount(pt, ptq.getQuantity());
    }

    static TravelerInfoSummary toTravelerInfoSummary(List<PassengerTypeCount> passengers) {
        TravelerInfoSummary travelerInfoSummary = new TravelerInfoSummary();
        AirTravelerAvail airTravelerAvail = new AirTravelerAvail();
        travelerInfoSummary.setAirTravelerAvail(Collections.singletonList(airTravelerAvail));
        airTravelerAvail.setPassengerTypeQuantity(
                passengers
                        .stream()
                        .map(p -> new PassengerTypeQuantity()
                                .withQuantity(p.getCount())
                                .withCode(toSabrePassengerCode(p)))
                        .collect(Collectors.toList())
        );
        return travelerInfoSummary;
    }

    // sabre-demo - this is the value from the sample. I'm assuming this would come from our Sabre agreement
    static POS createPOS() {
        return new POS().withSource(Collections.singletonList(
                new Source()
                        .withPseudoCityCode("PCC")
                        .withRequestorID(
                                new RequestorID()
                                        .withCompanyName(new CompanyName().withCode("TN"))
                                        .withID("REQ.ID").withType("0.AAA.X")
                        )));
    }

    static TPAExtensions_____ createIntelliSellTx() {
        return new TPAExtensions_____()
                .withIntelliSellTransaction(
                        new IntelliSellTransaction()
                                .withRequestType(new RequestType().withName("ADC1000")));
    }

    static void removeFakedOutRoundTripInboundResults(FlightOptions flightOptions, AirportCode origin) {
        logger.trace("Removing faked out return flights from Sabre Results: starting with {} options",
                flightOptions.getFlightOptions().size());
        List<FlightOption> outboundOnly = new ArrayList<>();
        Set<String> noDupes = new HashSet<>();
        for (FlightOption outbound : flightOptions.getFlightOptions()) {
            if (outbound.getSegments().get(0).getLegs().get(0).getOrigin().equals(origin)) {
                if (noDupes.add(outbound.getSegmentKeys().toString())) {
                    // cut the base fare in half (not bothering with the taxes for now)
                    cutPriceInHalf(outbound);
                    outbound.getProviderFares().get(0).setPackageId(PackageId.NONE);
                    outboundOnly.add(outbound);
                }
            }
        }
        // set the options back to remove the inbound flights
        flightOptions.setFlightOptions(outboundOnly);
        logger.trace("Removed faked out return flights from Sabre Results: ending with {} options",
                flightOptions.getFlightOptions().size());
    }

    private static FlightOption cutPriceInHalf(FlightOption flightOption) {
        assert flightOption.getProviderFares().size() == 1;
        ProviderFares pricingOption = flightOption.getProviderFares().get(0);
        // multiple fares aren't part of sabre yet.
        for (FareDetails fareDetails : pricingOption.getLowestNamedFare().getFareDetails()) {
            Money baseFare = fareDetails.getBaseFare();
            Money fakedOutOneWay = baseFare.divide(new BigDecimal("2"), MathContext.DECIMAL32);
            logger.trace("adjusting round trip price from {} to {}", baseFare, fakedOutOneWay);
            fareDetails.setBaseFare(fakedOutOneWay);
        }
        return flightOption;
    }

    static void removeFakedOutRoundTripOutboundResults(FlightOptions flightOptions, AirportCode outboundAirport) {
        logger.trace("Removing faked out outbound flights from Sabre Results: starting with {} options",
                flightOptions.getFlightOptions().size());

        // build a map of packageId to ProviderFares since we need to move these over to the return flights
        Map<PackageId, ProviderFares> packages = flightOptions.getFlightOptions()
                .stream()
                .filter(fo -> fo.getFirstOrigin().equals(outboundAirport))
                // cut the price in half since it currently reflects round trip
                .map(SabreUtils::cutPriceInHalf)
                .map(fo -> fo.getProviderFares().get(0))
                // mark the packageId as NONE when adding to the map, but still keyed by the original value
                .collect(Collectors.toMap(ProviderFares::getPackageId, pp -> pp.setPackageId(PackageId.NONE)));

        Set<String> noDupes = new HashSet<>();
        // walk the FlightOptions and remove all of the outbound flights
        // we'll de-dupe along the way
        List<FlightOption> inboundOnly = flightOptions.getFlightOptions()
                .stream()
                .filter(fo -> !fo.getFirstOrigin().equals(outboundAirport))
                .filter(fo -> noDupes.add(fo.getSegmentKeys().toString()))
                // set the pricing info on the inboundOnly based on their packageId value being sure to cut in half
                .peek(flightOption -> {
                    ArrayList<ProviderFares> providerFaresOptions = new ArrayList<>();
                    providerFaresOptions.add(packages.get(flightOption.getProviderFares().get(0).getPackageId()));
                    flightOption.setProviderFares(providerFaresOptions);
                })
                .collect(Collectors.toList());

        flightOptions.setFlightOptions(inboundOnly);

        logger.trace("Removed faked out outbound flights from Sabre Results: ending with {} options",
                flightOptions.getFlightOptions().size());
    }

    /**
     * Returns Sabre's configuration for flexibile date searches. This is something also supported in G4 but not many
     * other providers.
     * @param shopRequest
     * @return an extensions object with date flexibility configured or null
     */
    static TPAExtensions buildFlexibilityExtensions(FlexibleTravelRequest shopRequest) {
        TPAExtensions tpaExtensions = null;
        if (shopRequest.getReqMinusDays() > 0 || shopRequest.getReqPlusDays() > 0) {
            tpaExtensions = new TPAExtensions();

            DateFlexibility dateFlexibility = new DateFlexibility();
            tpaExtensions.setDateFlexibility(Collections.singletonList(dateFlexibility));

            if (shopRequest.getReqMinusDays() > 0) {
                dateFlexibility.setMinus(shopRequest.getReqMinusDays());
            }

            if (shopRequest.getReqPlusDays() > 0) {
                dateFlexibility.setPlus(shopRequest.getReqPlusDays());
            }
        }
        return tpaExtensions;
    }

    static TravelPreferences createTravelerPreferences(FlexibleTravelRequest shopRequest, int maxResults) {
        // We need to tell Sabre how many options to return. This is based on the hint from the user's request but we
        // also want to expand to include the results per date. For example, if the user says 20 max results, we'll
        // return 20 results PER DAY that they're searching.
        return new TravelPreferences()
                .withTPAExtensions(
                        new TPAExtensions_()
                                .withNumTrips(new NumTrips()
                                        .withNumber(maxResults *
                                                Math.max(1, shopRequest.getReqMinusDays() + shopRequest.getReqPlusDays()))
                                        .withPerDateMin(1)
                                        .withPerDateMax(maxResults)
                                )
                );
    }

    /**
     * Hits the Sabre Token Service to refresh our auth token. This is called by the TimerService
     * periodicaly to ensure the token is fresh and also whenever we see that the token is missing
     * or when we encounter a 401.
     * @throws TokenAuthException
     * @throws MalformedURLException
     */
    static TokenAuthResponse refreshToken() throws TokenAuthException, MalformedURLException {
        TokenAuthResponse authResponse = TokenAuthClient.call(new URL(SabreProperties.ENDPOINT.makeEndpoint(
                "/v2/auth/token")),
                SabreCredentials
                        .builder()
                        .setClientId(SabreProperties.API_CLIENTID.getOrThrow())
                        .setClientSecret(SabreProperties.API_SECRET.getOrThrow())
                        .build()
        );
        String accessToken = authResponse.getAccessToken();
        setAuthToken(accessToken);
        return authResponse;
    }

    // sabre-demo - remove if/when we enable all of sabre
    static boolean isAllowed(FlightShopRequest shopRequest) {
        String departingAirport = shopRequest.getDepartAirportCode().getValue();
        return "HNL".equals(departingAirport) || "BOS".equals(departingAirport);
    }

}
