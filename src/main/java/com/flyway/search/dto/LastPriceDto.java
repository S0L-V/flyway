package com.flyway.search.dto;

import lombok.Data;

@Data
public class LastPriceDto {
    private String flightId;
    private String flightNumber;
    private Long flightPrice;
}
