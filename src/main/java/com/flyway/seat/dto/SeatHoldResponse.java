package com.flyway.seat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SeatHoldResponse {
    private String reservationId;
    private String reservationSegmentId;
    private String flightId;

    private String passengerId;

    private String flightSeatId; // PK
    private String seatNo; // 12A
    private String cabinClassCode; // ECO/BIZ/FST

    private String seatStatus; // HOLD
    private LocalDateTime holdExpiresAt;

    private LocalDateTime serverTime;
}
