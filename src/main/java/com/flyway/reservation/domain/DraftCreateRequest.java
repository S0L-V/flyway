package com.flyway.reservation.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DraftCreateRequest {
    private String OutFlightId;
    private String InFlightId;
    private String CabinClassCode;
    private int PassengerCount;
}
