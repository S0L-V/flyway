package com.flyway.search.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightSearchRequest {
    private String tripType;
    private String from;
    private String to;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private Integer passengers;
    private String cabinClass;
}
