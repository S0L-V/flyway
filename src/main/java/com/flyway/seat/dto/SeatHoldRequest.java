package com.flyway.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SeatHoldRequest {
    private String passengerId; // 필수
    private String seatNo; // 필수 ex) 12A
}
