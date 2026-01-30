package com.flyway.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiredReservationView {

    private String reservationId;
    private int passengerCount;
    private String flightId;
    private String cabinClassCode;

}