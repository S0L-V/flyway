package com.flyway.search.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightSearchResponseDTO {
    private String flightId;
    private String departureAirport;
    private String arrivalAirport;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String flightNumber;
    private int seatCount;
}
