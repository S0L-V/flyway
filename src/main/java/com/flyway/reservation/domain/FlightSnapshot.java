package com.flyway.reservation.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightSnapshot  {

    String flightId;
    String departureTime;
    String arrivalTime;
    String departureAirport;
    String arrivalAirport;
    String flightNumber;

}
