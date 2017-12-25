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

import com.allegianttravel.ota.flight.shop.g4.markets.MarketService;
import com.allegianttravel.ota.flight3.MarketName;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of a cache aware MarketService. Built using a cache api and a MarketService implementation to
 * lazily load the values
 */
public class CachedMarketServiceImpl implements MarketService {

    private static final Logger logger = LoggerFactory.getLogger(CachedMarketServiceImpl.class);

    private final Map<MarketName, Boolean> cache;

    CachedMarketServiceImpl(MarketService injectedMarketService) {
        ExpiringMap<MarketName, Boolean> expiringMap = ExpiringMap.builder()
                .expiration(1, TimeUnit.HOURS)
                .entryLoader(injectedMarketService::isAllowed)
                .build();
        expiringMap.addAsyncExpirationListener((key, value) -> expiringMap.put(key, injectedMarketService.isAllowed(key)));
        cache = expiringMap;
    }

    @Override
    public boolean isAllowed(MarketName marketName) {
        Boolean result = cache.get(marketName);
        logger.debug("market {} isAllowed()={}", marketName.getName(), result);
        return result;
    }
}
