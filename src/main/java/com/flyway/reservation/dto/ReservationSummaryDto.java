package com.flyway.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryDto {

    /* 예약 기본 정보 */
    private String reservationId;
    private String reservationStatus;
    private LocalDateTime reservedAt;

    /* 결제 정보 */
    private Integer paidAmount;

    /* 가는 편 (OUTBOUND) */
    private String outFlightId;

    private String outDepartureAirport;
    private String outDepartureCity;

    private String outArrivalAirport;
    private String outArrivalCity;

    private LocalDateTime outDepartureTime;
    private LocalDateTime outArrivalTime;

    /* 오는 편 (INBOUND) */
    private String inFlightId;

    private String inDepartureAirport;
    private String inDepartureCity;

    private String inArrivalAirport;
    private String inArrivalCity;

    private LocalDateTime inDepartureTime;
    private LocalDateTime inArrivalTime;
}

