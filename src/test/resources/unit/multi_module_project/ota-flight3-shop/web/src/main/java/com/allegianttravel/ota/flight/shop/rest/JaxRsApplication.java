/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.rest;

import com.allegiant.commons.jaxrs.validation.JaxRsMethodValidationExceptionMapper;
import com.allegiant.util.gen2.jaxrs.DefaultJacksonJaxbJsonProvider;
import com.allegianttravel.ota.flight.shop.rest.impl.FlightShopResourceImpl;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationPath("/api")
public class JaxRsApplication extends Application {

    public JaxRsApplication() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("Allegiant OTA Flight3 Shop");
        beanConfig.setVersion("3.0-SNAPSHOT");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setBasePath("ota-flight-shop/v3/api");
        beanConfig.setResourcePackage(JaxRsApplication.class.getPackage().getName());
        beanConfig.setScan(false);
        beanConfig.setPrettyPrint(true);

    }

    @Override
    public Set<Class<?>> getClasses() {

        return Stream.of(
                JaxRsMethodValidationExceptionMapper.class,
                FlightShopResourceImpl.class,
                DefaultJacksonJaxbJsonProvider.class,
                RestEasyFailureExceptionMapper.class,
                ApiListingResource.class,  // required for Swagger
                SwaggerSerializers.class  // required for Swagger
        )
                .collect(Collectors.toSet());
    }
}
