package com.flyway.seat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SeatReleaseResponse {
    private String reservationId;
    private String reservationSegmentId;
    private String flightId;

    private String passengerId;

    private String releasedFlightSeatId; // 해제된 flight_seat_id(없으면 null)
    private String seatStatus; // AVAILABLE
    private LocalDateTime serverTime;

}
