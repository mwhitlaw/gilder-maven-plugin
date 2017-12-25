package com.allegianttravel.ota.flight.shop.qpx;

import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.provider.PackageSupplier;
import com.allegianttravel.ota.flight.shop.provider.Profile;
import com.allegianttravel.ota.flight.shop.provider.ProviderSystemException;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.framework.module.spi.Provider;
import com.allegianttravel.ota.framework.module.spi.ProviderInput;
import com.allegianttravel.ota.framework.module.spi.ProviderOutput;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.qpxExpress.QPXExpress;
import com.google.api.services.qpxExpress.QPXExpressRequestInitializer;
import com.google.api.services.qpxExpress.model.PassengerCounts;
import com.google.api.services.qpxExpress.model.SliceInput;
import com.google.api.services.qpxExpress.model.TripOptionsRequest;
import com.google.api.services.qpxExpress.model.TripsSearchRequest;
import com.google.api.services.qpxExpress.model.TripsSearchResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.allegianttravel.ota.flight.shop.provider.FlightShopUtils.toProviderResponse;

@Named
@Profile
@ApplicationScoped
public class QpxFlightShopProvider implements Provider {

    private HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Inject
    public QpxFlightShopProvider(@ProductionTransport HttpTransport httpTransport) {
        this.httpTransport = httpTransport;
    }

    /**
     * Needed for CDI
     */
    @SuppressWarnings("unused")
    public QpxFlightShopProvider() {}

    @Override
    public ProviderOutput provide(ProviderInput providerInput) {
        FlightShopRequest shopRequest = (FlightShopRequest) providerInput;
        FlightOptions options = query(shopRequest);
        return toProviderResponse(shopRequest, options);
    }

    @Override
    public int clearCaches() {
        return 0;
    }


    private FlightOptions query(FlightShopRequest flightRequest) {
        PassengerCounts passengers= new PassengerCounts();
        passengers.setAdultCount(flightRequest.getPassengerCount(p-> p.getPassengerType() == PassengerType.ADULT));
        passengers.setSeniorCount(flightRequest.getPassengerCount(p-> p.getPassengerType() == PassengerType.SENIOR));
        passengers.setChildCount(flightRequest.getPassengerCount(p-> p.getPassengerType() == PassengerType.CHILD));
        passengers.setInfantInSeatCount(flightRequest.getPassengerCount(p-> p.getPassengerType() == PassengerType.INFANT));
        passengers.setInfantInSeatCount(flightRequest.getPassengerCount(p-> p.getPassengerType() == PassengerType.INFANT_ON_LAP));

        List<SliceInput> slices = new ArrayList<>();

        SliceInput outbound = new SliceInput();
        outbound.setOrigin(flightRequest.getDepartAirportCode().getValue());
        outbound.setDestination(flightRequest.getArriveAirportCode().getValue());
        outbound.setDate(flightRequest.getDepartDate().toString());
        slices.add(outbound);

        if (flightRequest.getReturnDate() != null) {
            SliceInput inbound = new SliceInput();
            inbound.setOrigin(flightRequest.getArriveAirportCode().getValue());
            inbound.setDestination(flightRequest.getDepartAirportCode().getValue());
            inbound.setDate(flightRequest.getReturnDate().toString());
            slices.add(inbound);
        }

        TripOptionsRequest request= new TripOptionsRequest();
        request.setSolutions(flightRequest.getMaxResults());
        request.setPassengers(passengers);
        request.setSlice(slices);

        TripsSearchRequest parameters = new TripsSearchRequest();
        parameters.setRequest(request);

        QPXExpress qpXExpress= new QPXExpress.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(QpxProperties.APPLICATION_NAME.getOrThrow())
                .setGoogleClientRequestInitializer(new QPXExpressRequestInitializer(QpxProperties.API_KEY.getOrThrow()))
                .build();

        try {
            TripsSearchResponse list = qpXExpress.trips().search(parameters).execute();
            return new QpxTripOptionToFlightOptionMapper(flightRequest, list, new PackageSupplier(getName())).map();
        } catch(IOException e) {
            throw new ProviderSystemException(e);
        }
    }

    @Override
    public String getName() {
        return "qpx";
    }

    @Override
    public String getDescription() {
        return "QPX Express";
    }
}
