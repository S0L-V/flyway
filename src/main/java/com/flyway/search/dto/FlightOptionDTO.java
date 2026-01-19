package com.flyway.search.dto;

import lombok.Data;

@Data
public class FlightOptionDTO {
    private FlightSearchResponseDTO outbound;
    private FlightSearchResponseDTO inbound;
    private int totalPrice;
}
