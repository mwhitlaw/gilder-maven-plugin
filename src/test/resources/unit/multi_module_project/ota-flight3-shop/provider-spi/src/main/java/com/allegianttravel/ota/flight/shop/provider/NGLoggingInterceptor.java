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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor that logs the timings for the method being invoked in
 * an slf4j logger.
 */
@Interceptor
@Profile
public class NGLoggingInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        String className = context.getTarget().getClass().getName();
        String methodName = context.getMethod().getName();

        String unwrappedClassName;
        int index = className.indexOf('$');
        if (index == -1) {
            unwrappedClassName = className;
        } else {
            unwrappedClassName = className.substring(0, index);
        }

        Logger logger = LoggerFactory.getLogger("timing." + unwrappedClassName);
        Object result;

        long time = System.currentTimeMillis();
        try {
            result = context.proceed();
        } finally {
            long elapsedTime = System.currentTimeMillis() - time;
            logger.debug("{}.{} took {} millis", unwrappedClassName, methodName, TimeUnit.MILLISECONDS.toMillis(elapsedTime));
        }
        return result;
    }
}
