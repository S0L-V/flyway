package com.flyway.search.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightVO {
    private String flightId;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String flightNumber;
}
