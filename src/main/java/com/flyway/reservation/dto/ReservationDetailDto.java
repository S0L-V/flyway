package com.flyway.reservation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationDetailDto {

    /* Reservation */
    private String reservationId;
    private String userId;
    private String reservationStatus;
    private LocalDateTime reservedAt;
    private int passengerCount;
    private String tripType; // 0 편도 | 1 왕복

    /* Payment summary (latest PAID) */
    private Long paidAmount;
    private String paymentMethod;
    private String paymentStatus; // TODO: PaymentStatus ENUM 적용
    private LocalDateTime paidAt;

    /* OUTBOUND */
    private String outFlightId;
    private String outFlightNumber;
    private String outCabinClassCode;
    private Long outPricePerPerson;

    private String outDepartureAirportId;
    private String outDepartureCity;
    private String outDepartureCountry;

    private String outArrivalAirportId;
    private String outArrivalCity;
    private String outArrivalCountry;

    private LocalDateTime outDepartureTime;
    private LocalDateTime outArrivalTime;

    private String outTerminalNo;
    private Integer outDurationMinutes;

    /* INBOUND (segment_order = 2) */
    private String inFlightId;
    private String inFlightNumber;
    private String inCabinClassCode;
    private Long inPricePerPerson;

    private String inDepartureAirportId;
    private String inDepartureCity;
    private String inDepartureCountry;

    private String inArrivalAirportId;
    private String inArrivalCity;
    private String inArrivalCountry;

    private LocalDateTime inDepartureTime;
    private LocalDateTime inArrivalTime;

    private String inTerminalNo;
    private Integer inDurationMinutes;
}
