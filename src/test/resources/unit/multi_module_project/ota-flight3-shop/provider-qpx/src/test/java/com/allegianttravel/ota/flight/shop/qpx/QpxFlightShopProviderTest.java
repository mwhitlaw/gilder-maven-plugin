package com.allegianttravel.ota.flight.shop.qpx;

import com.allegiant.util.gen2.JsonUtils;
import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.api.services.qpxExpress.model.TripsSearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.allegiant.tests.LoadFromClasspath.asStream;
import static com.allegiant.tests.LoadFromClasspath.asString;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class QpxFlightShopProviderTest {

    private final FlightShopRequest request;
    private final String input;
    private final String expected;

    public QpxFlightShopProviderTest(@SuppressWarnings("unused") String name,
                                     FlightShopRequest request,
                                     String input, String expected) {
        this.input = input;
        this.expected = expected;
        this.request = request;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() throws Exception {
        FlightShopRequest.Builder builder = FlightShopRequest.builder()
                .setPassengers(new ArrayList<>())
                .setDepartAirportCode(new AirportCode("BOS"))
                .setArriveAirportCode(new AirportCode("LAS"));
        return Arrays.asList(
                new Object[] {
                        "oneway",
                        builder.setReturnDate(null).build(),
                        "/qpx-shop-single-tripOption.json",
                        "/expected-single-tripOption.json"},
                new Object[] {"roundtrip, 2 passengers",
                        builder.setReturnDate(LocalDate.now().plusDays(10)).build(),
                        "/qpx-roundtrip-2-adults.json",
                        "/expected-roundtrip.json"}
                );
    }


    @Test
    public void test() throws Exception {
        TripsSearchResponse response = QpxFlightShopProvider.JSON_FACTORY.fromInputStream(
                asStream(input), TripsSearchResponse.class);
        assertNotNull(response);

        QpxTripOptionToFlightOptionMapper mapper = new QpxTripOptionToFlightOptionMapper(request, response, new Supplier<PackageId>() {

            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public PackageId get() {
                return new PackageId("package-" + counter.getAndIncrement());
            }
        });

        FlightOptions flightOptions = mapper.map();
        assertNotNull(flightOptions);

        ObjectMapper om = JsonUtils.defaultJacksonObjectMapper();
        om.findAndRegisterModules();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        String actual = om.writeValueAsString(flightOptions);
        System.out.println(actual);

        assertJsonEquals(asString(expected),actual);
    }
}
