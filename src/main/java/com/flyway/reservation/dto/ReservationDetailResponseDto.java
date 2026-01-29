package com.flyway.reservation.dto;

import java.time.LocalDateTime;

import com.flyway.reservation.domain.TripType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetailResponseDto {
    private ReservationInfo reservation;
    private Itinerary itinerary;
    private PaymentInfo payment;

    public static ReservationDetailResponseDto from(ReservationDetailDto detail) {
        if (detail == null) return null;
        return ReservationDetailResponseDto.builder()
                .reservation(ReservationInfo.from(detail))
                .itinerary(Itinerary.from(detail))
                .payment(PaymentInfo.from(detail))
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationInfo {
        private String reservationId;
        private String status;
        private LocalDateTime reservedAt;
        private int passengerCount;
        private TripType tripType;

        public static ReservationInfo from(ReservationDetailDto detail) {
            return ReservationInfo.builder()
                    .reservationId(detail.getReservationId())
                    .status(detail.getReservationStatus())
                    .reservedAt(detail.getReservedAt())
                    .passengerCount(detail.getPassengerCount())
                    .tripType(TripType.from(detail.getTripType()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Itinerary {
        private Segment outbound;
        private Segment inbound;

        public static Itinerary from(ReservationDetailDto detail) {
            return Itinerary.builder()
                    .outbound(Segment.outbound(detail))
                    .inbound(Segment.inbound(detail))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Segment {
        private String flightId;
        private String flightNumber;
        private String cabinClass;
        private Long pricePerPerson;
        private AirportInfo departure;
        private AirportInfo arrival;
        private String terminalNo;
        private Integer durationMinutes;

        private static Segment outbound(ReservationDetailDto detail) {
            if (detail.getOutFlightId() == null) return null;
            return Segment.builder()
                    .flightId(detail.getOutFlightId())
                    .flightNumber(detail.getOutFlightNumber())
                    .cabinClass(detail.getOutCabinClassCode())
                    .pricePerPerson(detail.getOutPricePerPerson())
                    .departure(AirportInfo.outboundDeparture(detail))
                    .arrival(AirportInfo.outboundArrival(detail))
                    .terminalNo(detail.getOutTerminalNo())
                    .durationMinutes(detail.getOutDurationMinutes())
                    .build();
        }

        private static Segment inbound(ReservationDetailDto detail) {
            if (detail.getInFlightId() == null) return null;
            return Segment.builder()
                    .flightId(detail.getInFlightId())
                    .flightNumber(detail.getInFlightNumber())
                    .cabinClass(detail.getInCabinClassCode())
                    .pricePerPerson(detail.getInPricePerPerson())
                    .departure(AirportInfo.inboundDeparture(detail))
                    .arrival(AirportInfo.inboundArrival(detail))
                    .terminalNo(detail.getInTerminalNo())
                    .durationMinutes(detail.getInDurationMinutes())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AirportInfo {
        private String airportId;
        private String city;
        private String country;
        private LocalDateTime time;

        private static AirportInfo outboundDeparture(ReservationDetailDto detail) {
            return AirportInfo.builder()
                    .airportId(detail.getOutDepartureAirportId())
                    .city(detail.getOutDepartureCity())
                    .country(detail.getOutDepartureCountry())
                    .time(detail.getOutDepartureTime())
                    .build();
        }

        private static AirportInfo outboundArrival(ReservationDetailDto detail) {
            return AirportInfo.builder()
                    .airportId(detail.getOutArrivalAirportId())
                    .city(detail.getOutArrivalCity())
                    .country(detail.getOutArrivalCountry())
                    .time(detail.getOutArrivalTime())
                    .build();
        }

        private static AirportInfo inboundDeparture(ReservationDetailDto detail) {
            return AirportInfo.builder()
                    .airportId(detail.getInDepartureAirportId())
                    .city(detail.getInDepartureCity())
                    .country(detail.getInDepartureCountry())
                    .time(detail.getInDepartureTime())
                    .build();
        }

        private static AirportInfo inboundArrival(ReservationDetailDto detail) {
            return AirportInfo.builder()
                    .airportId(detail.getInArrivalAirportId())
                    .city(detail.getInArrivalCity())
                    .country(detail.getInArrivalCountry())
                    .time(detail.getInArrivalTime())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String status;
        private Long paidAmount;
        private String method;
        private LocalDateTime paidAt;

        public static PaymentInfo from(ReservationDetailDto detail) {
            return PaymentInfo.builder()
                    .status(detail.getPaymentStatus())
                    .paidAmount(detail.getPaidAmount())
                    .method(detail.getPaymentMethod())
                    .paidAt(detail.getPaidAt())
                    .build();
        }
    }
}
