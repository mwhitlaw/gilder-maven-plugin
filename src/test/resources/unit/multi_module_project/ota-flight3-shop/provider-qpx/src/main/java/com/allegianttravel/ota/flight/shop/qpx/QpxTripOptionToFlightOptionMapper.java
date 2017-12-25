package com.allegianttravel.ota.flight.shop.qpx;

import com.allegianttravel.ota.flight.shop.provider.FlightShopRequest;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOption;
import com.allegianttravel.ota.flight.shop.rest.dto.FlightOptions;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerTypeCount;
import com.allegianttravel.ota.flight.shop.rest.dto.PassengerType;
import com.allegianttravel.ota.flight.shop.rest.dto.price.NamedFare;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PackageId;
import com.allegianttravel.ota.flight.shop.rest.dto.price.PricingInfo;
import com.allegianttravel.ota.flight.shop.rest.dto.price.ProviderPricing;
import com.allegianttravel.ota.flight.shop.rest.dto.price.Tax;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AircraftCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirlineCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.AirportCode;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.ConnectionInfo;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.FlightNumber;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Leg;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Segment;
import com.allegianttravel.ota.flight.shop.rest.dto.travel.Terminal;
import com.allegianttravel.ota.flight.shop.types.Money;
import com.google.api.services.qpxExpress.model.LegInfo;
import com.google.api.services.qpxExpress.model.PassengerCounts;
import com.google.api.services.qpxExpress.model.SegmentInfo;
import com.google.api.services.qpxExpress.model.SliceInfo;
import com.google.api.services.qpxExpress.model.TripOption;
import com.google.api.services.qpxExpress.model.TripsSearchResponse;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class QpxTripOptionToFlightOptionMapper {

    private final FlightShopRequest request;
    private final TripsSearchResponse response;
    private final Supplier<PackageId> packageIdSupplier;


    QpxTripOptionToFlightOptionMapper(FlightShopRequest request, TripsSearchResponse response, Supplier<PackageId> packageIdSupplier) {
        this.request = request;
        this.response = response;
        this.packageIdSupplier = packageIdSupplier;
    }

    FlightOptions map() {
        FlightOptions options = new FlightOptions();
        return options.setFlightOptions(
                response.getTrips().getTripOption()
                        .stream()
                        .map(this::mapQpxToFlightOption)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }

    private List<FlightOption> mapQpxToFlightOption(TripOption tripOption) {

        List<FlightOption> list = new ArrayList<>();

        PackageId packageId = null;

        boolean outbound = true;

        for(SliceInfo slice : tripOption.getSlice()) {
            FlightOption currentFlightOption = new FlightOption().setSegments(new ArrayList<>());
            list.add(currentFlightOption);

            for(SegmentInfo qpxSegment : slice.getSegment()) {
                Segment segment = new Segment();

                // the directionality changes from OUTBOUND to INBOUND as soon as we see that the departure airport is our
                // original destination. This check is only necessary for round trip requests.
                if (outbound &&
                        request.isRoundTrip() &&
                        qpxSegment.getLeg().get(0).getOrigin().equals(this.request.getArriveAirportCode().getValue())) {
                    outbound = false;

                    // if this is not the first qpxSegment, then we're splitting a tripOption into
                    // 2 different FlightOptions
                    if (qpxSegment != slice.getSegment().get(0)) {
                        currentFlightOption = new FlightOption().setSegments(new ArrayList<>());
                        list.add(currentFlightOption);
                    }
                }

                segment.setFlightNumber(new FlightNumber(qpxSegment.getFlight().getNumber()));
                segment.setCarrierCode(new AirlineCode(qpxSegment.getFlight().getCarrier()));
                if (qpxSegment.getConnectionDuration() != null) {
                    segment.setConnectionInfo(new ConnectionInfo()
                            .setConnectionDuration(qpxSegment.getConnectionDuration())
                            .setChangePlanes(true)
                    );
                }

                List<Leg> legs = new ArrayList<>();
                for(LegInfo qpxLeg : qpxSegment.getLeg()) {
                    Leg leg = new Leg();

                    if (qpxLeg.getConnectionDuration() != null) {
                        leg.setConnectionInfo(new ConnectionInfo()
                                .setConnectionDuration(qpxLeg.getConnectionDuration())
                                .setChangePlanes(qpxLeg.getChangePlane())
                        );
                    }
                    if (qpxLeg.getOperatingDisclosure() != null) {
                        leg.setOperatorCode(new AirlineCode(qpxLeg.getOperatingDisclosure()));
                    }
                    leg.setAircraftCode(AircraftCode.of(qpxLeg.getAircraft()));
                    leg.setArrivalTime(LocalDateTime.from(ZonedDateTime.parse(qpxLeg.getArrivalTime())));
                    leg.setDepartureTime(LocalDateTime.from(ZonedDateTime.parse(qpxLeg.getDepartureTime())));
                    leg.setDestination(new AirportCode(qpxLeg.getDestination()));
                    leg.setOrigin(new AirportCode(qpxLeg.getOrigin()));
                    if (qpxLeg.getDestinationTerminal() != null) {
                        leg.setDestinationTerminal(new Terminal(qpxLeg.getDestinationTerminal()));
                    }
                    if (qpxLeg.getOriginTerminal() != null) {
                        leg.setOriginTerminal(new Terminal(qpxLeg.getOriginTerminal()));
                    }
                    leg.setDuration(qpxLeg.getDuration());
                    leg.setMiles(qpxLeg.getMileage());
                    leg.setOnTimePerformance(qpxLeg.getOnTimePerformance());

                    legs.add(leg);
                }
                segment.setLegs(legs);
                currentFlightOption.getSegments().add(segment);
            }

            ProviderPricing pp = new ProviderPricing()
                    .setFareClassCodes(slice.getSegment().stream().map(SegmentInfo::getBookingCode).collect(Collectors.toList()))
                    .setProviderId("qpx");
            // if there are multiple slices, then the price is a package deal and we
            // need to record the price of only the first one
            if (packageId == null) {
                packageId = tripOption.getSlice().size() > 1 ? packageIdSupplier.get() : PackageId.NONE;
                currentFlightOption.setProviderPricingOptions(
                        Collections.singletonList(
                                pp.setPackageId(packageId)
                                        .addNamedFare(new NamedFare(ProviderPricing.RACK_RATE, tripOption.getPricing()
                                                .stream()
                                                .map(QpxTripOptionToFlightOptionMapper::mapPricingInfo)
                                                .collect(Collectors.toList())
                                        ))));
            } else {
                currentFlightOption.setProviderPricingOptions(
                        Collections.singletonList(pp.setPackageId(packageId))
                );
            }
        } // end slice


        return list;
    }

    private static PricingInfo mapPricingInfo(com.google.api.services.qpxExpress.model.PricingInfo qpxPi) {
        PricingInfo pi = new PricingInfo();

        // todo missing: availableSeats, fees

        pi.setBaseFare(new Money(qpxPi.getBaseFareTotal()));
        pi.setTaxes(
                qpxPi.getTax()
                        .stream()
                        .map(ti->new Tax()
                                .setCode(ti.getCode())
                                .setAmount(new Money(ti.getSalePrice()))
                        )
                        .collect(Collectors.toList())
        );
        pi.setPassengers(toPassengers(qpxPi.getPassengers()));
        return pi;
    }

    private static List<PassengerTypeCount> toPassengers(PassengerCounts qpxPassenger) {
        List<PassengerTypeCount> list = new ArrayList<>();
        if (qpxPassenger.getSeniorCount() != null) {
            list.add(new PassengerTypeCount(PassengerType.SENIOR, qpxPassenger.getSeniorCount()));
        }
        if (qpxPassenger.getAdultCount() != null) {
            list.add(new PassengerTypeCount(PassengerType.ADULT, qpxPassenger.getAdultCount()));
        }
        if (qpxPassenger.getChildCount() != null) {
            list.add(new PassengerTypeCount(PassengerType.CHILD, qpxPassenger.getChildCount()));
        }
        if (qpxPassenger.getInfantInSeatCount() != null) {
            list.add(new PassengerTypeCount(PassengerType.INFANT, qpxPassenger.getInfantInSeatCount()));
        }
        if (qpxPassenger.getInfantInLapCount() != null) {
            list.add(new PassengerTypeCount(PassengerType.INFANT_ON_LAP, qpxPassenger.getInfantInLapCount()));
        }
        return list;
    }
}
