package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.SearchResultDto;

import java.util.List;

public interface FlightService {
    List<Flight> list(Flight vo);
    List<Airport> depAirport(Airport vo);
    List<Airport> arrAirport(String depAirport);
    List<Airline> airline(Airline vo);
    SearchResultDto search(FlightSearchRequest dto);
    FlightDetailDto details(String cabinClass, String routeType);
}
