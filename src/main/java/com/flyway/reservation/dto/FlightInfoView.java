package com.flyway.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInfoView {

    private String flightInfoId;
    private String flightId;
    private Integer firstClassSeat;
    private Integer businessClassSeat;
    private Integer economyClassSeat;
}