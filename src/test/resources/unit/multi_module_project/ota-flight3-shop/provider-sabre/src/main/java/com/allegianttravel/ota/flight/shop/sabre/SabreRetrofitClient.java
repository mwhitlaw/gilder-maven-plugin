/*
 * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package com.allegianttravel.ota.flight.shop.sabre;

import com.allegiant.commons.retrofit.ServiceGenerator;
import com.allegiant.commons.retrofit.Settings;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQ;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRQV1951Schema;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRS;
import com.allegianttravel.sabre.generated.OTAAirLowFareSearchRSV1951Schema;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

public final class SabreRetrofitClient {

    private static final Logger logger = LoggerFactory.getLogger(SabreRetrofitClient.class);

    private SabreRetrofitClient() {

    }

    public static <T> T createSabreServiceProxy(String endpoint, Class<T> type) {

        return ServiceGenerator.create(type,
                endpoint,
                Settings.builder()
                        .setInterceptor(chain -> {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + SabreUtils.getAuthToken())
                                    .build();
                            return chain.proceed(newRequest);
                        })
                        .build());
    }

    public static <T> T callSabreWithAuthRetry(Call<T> call) throws SabreApplicationException, SabreAuthException {
        try {
            return callSabre(call);
        } catch (SabreAuthException e) {
            // todo - left over from the SabreRetryClient
            SabreUtils.AUTH_TOKEN_FUNCTIONS.clearAuthToken();
            return callSabre(call);
        }
    }

    public static <T> T callSabre(Call<T> call) throws SabreAuthException {
        T retVal;
        try {

            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                retVal = response.body();
            } else if (response.code() == javax.ws.rs.core.Response.Status.FORBIDDEN.getStatusCode()) {
                throw new SabreAuthException();
            } else {
                throw new SabreApplicationException("non-auth error from sabre", null, response.code(), null);
            }
        } catch (IOException e) {
            logger.debug("Error invoking sabre service for {}", call.request().url());
            retVal = null;
        }
        return retVal;
    }

    static Optional<OTAAirLowFareSearchRS> search(OTAAirLowFareSearchRQ searchRQ) throws IOException {

        String endpoint = SabreProperties.ENDPOINT.makeEndpoint("/");
        SabreCalendarFlightsService searchService = SabreRetrofitClient.createSabreServiceProxy(endpoint,
                SabreCalendarFlightsService.class);

        OTAAirLowFareSearchRQV1951Schema payload = new OTAAirLowFareSearchRQV1951Schema()
                .withOTAAirLowFareSearchRQ(searchRQ);

        Call<OTAAirLowFareSearchRSV1951Schema> call = searchService.search(payload);

        OTAAirLowFareSearchRS retVal;
        try {
            OTAAirLowFareSearchRSV1951Schema response = SabreRetrofitClient.callSabreWithAuthRetry(call);
            retVal = response.getOTAAirLowFareSearchRS();
        } catch (SabreAuthException e) {
            logger.debug("Error invoking sabre {}", call.request().url(), e);
            retVal = null;
        }

        return Optional.ofNullable(retVal);
    }

}
