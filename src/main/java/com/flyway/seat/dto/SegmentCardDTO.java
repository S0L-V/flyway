package com.flyway.seat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SegmentCardDTO {
    private String reservationSegmentId;
    private String depAirportCode;
    private String arrAirportCode;
    private LocalDateTime depTime;

    public String getDepTimeText() {
        if (depTime == null) return "";
        return depTime.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
    }
}
