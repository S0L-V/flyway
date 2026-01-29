package com.flyway.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerSeatInfo {
    private String passengerId;
    private String passengerName;
    private String seatNo;         // 12A
}