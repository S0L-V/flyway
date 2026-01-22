package com.flyway.reservation.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DraftCreateRequest {
    private String outFlightId;
    private String inFlightId;
    private int passengerCount;
    private String cabinClassCode;
}
