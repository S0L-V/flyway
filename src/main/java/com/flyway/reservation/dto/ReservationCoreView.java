package com.flyway.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCoreView {

    private String reservationId;
    private String userId;

    private int passengerCount;
    private String status;         // HELD/CONFIRMED/EXPIRED
    private LocalDateTime expiredAt;
}
