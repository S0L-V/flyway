package com.flyway.search.repository;

import com.flyway.search.domain.Airline;
import com.flyway.search.domain.Airport;
import com.flyway.search.domain.Flight;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.FlightSearchResponse;
import com.flyway.search.mapper.FlightMapper;

import java.util.List;

public interface FlightRepository {
    List<Flight> findAll(Flight vo);

    List<Airport> findDepAirports(Airport vo);

    List<Airport> findArrAirports(String depAirport);

    List<Airline> findAirlines(Airline vo);

    List<FlightSearchResponse> findOutboundFlights(FlightSearchRequest dto);

    List<FlightSearchResponse> findInboundFlights(FlightSearchRequest dto);

    FlightDetailDto findDetails(String cabinClass, String routeType);
}
