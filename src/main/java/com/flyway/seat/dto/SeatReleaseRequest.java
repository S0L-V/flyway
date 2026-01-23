package com.flyway.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SeatReleaseRequest {
    private String passengerId; // 어떤 승객의 좌석을 해제할지
}
