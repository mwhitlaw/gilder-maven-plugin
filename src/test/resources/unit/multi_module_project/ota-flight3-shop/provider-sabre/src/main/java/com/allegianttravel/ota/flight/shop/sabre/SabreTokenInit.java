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

import com.allegiant.sabre.TokenAuthException;
import com.allegiant.sabre.TokenAuthResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for initializing the Sabre Auth Token from their token service. This token needs to set before any of
 * the calls to Sabre are done since they all rely on having the auth token configured.
 */
@Startup
@Singleton
public class SabreTokenInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(SabreTokenInit.class);
    private static final int DURATION = 30;

    @Resource
    private TimerService timerService;

    @PostConstruct
    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void refreshToken() {
        long refreshCountdown;
        try {
            TokenAuthResponse authResponse = SabreUtils.refreshToken();
            LOGGER.debug("Got new Sabre Auth Token {}", authResponse);

            // perform the refresh 5 minutes before the expiration
            refreshCountdown = TimeUnit.SECONDS.toMillis(authResponse.getExpiresIn() -
                    TimeUnit.MINUTES.toSeconds(5));

            // sanity check that the refreshCountdown hasn't been changed by Sabre to be less than 5 minutes
            refreshCountdown = Math.max(TimeUnit.MINUTES.toMillis(5), refreshCountdown);

        } catch (MalformedURLException | TokenAuthException e) {
            LOGGER.error("Error getting sabre auth token", e);
            // retry in 30 seconds
            refreshCountdown = TimeUnit.SECONDS.toMillis(DURATION);
        }

        LOGGER.debug("Refreshing the sabre token in {} millis which should be {}", refreshCountdown,
                new DateTime().plusMillis((int) refreshCountdown));

        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        timerService.createSingleActionTimer(refreshCountdown, timerConfig);
    }
}
