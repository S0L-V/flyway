package com.flyway.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundSegmentDto {
    private String flightId;
    private String cabinClass;
}