package com.flyway.search.dto;

import lombok.Data;

@Data
public class FlightOptionDto {
    private FlightSearchResponse outbound;
    private FlightSearchResponse inbound;
    private int totalPrice;
    private int totalSeats;
}
