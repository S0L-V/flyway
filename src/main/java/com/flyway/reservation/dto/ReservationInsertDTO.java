package com.flyway.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationInsertDTO {

    private String reservationId;
    private String userId;
    private LocalDateTime reservedAt;
    private String status;
    private int passengerCount;
    private String tripType;
    private LocalDateTime expiredAt;

}
