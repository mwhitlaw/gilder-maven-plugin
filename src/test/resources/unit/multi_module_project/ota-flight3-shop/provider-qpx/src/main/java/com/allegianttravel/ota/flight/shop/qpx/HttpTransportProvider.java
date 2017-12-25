package com.allegianttravel.ota.flight.shop.qpx;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class HttpTransportProvider {

    @Produces
    @ProductionTransport
    public HttpTransport produceTransport() {
        return new NetHttpTransport();
    }

}
