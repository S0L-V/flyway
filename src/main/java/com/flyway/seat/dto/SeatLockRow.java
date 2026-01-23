package com.flyway.seat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SeatLockRow {
    private String flightSeatId;
    private String flightId;
    private String aircraftSeatId;

    private String seatNo;
    private String cabinClassCode;

    private String seatStatus;                // AVAILABLE/HOLD/BOOKED or null
    private String holdReservationSegmentId;  // flight_seat.hold_reservation_segment_id
    private LocalDateTime holdExpiresAt;
}
