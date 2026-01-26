package com.flyway.seat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class SeatDTO {
    private String flightSeatId;
    private String flightId;

    private String seatNo;
    private Integer rowNo;
    private String colNo;

    private String cabinClassCode;
    private String seatStatus;

    private LocalDateTime holdExpiresAt;


}