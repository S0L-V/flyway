package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.LastPriceDto;
import com.flyway.search.dto.SearchResultDto;

import java.util.List;
import java.util.Map;

public interface FlightService {
    List<Flight> list(Flight vo);
    List<Airport> depAirport(Airport vo);
    List<Airport> arrAirport(String depAirport);
    List<Airline> airline(Airline vo);
    SearchResultDto search(FlightSearchRequest dto);
    FlightDetailDto details(String cabinClass, String routeType);
    List<LastPriceDto> findPrice(String outFlightId, String inFlightId, String cabinClassCode);
    List<Map<String, Object>> findPriceHistory(String flightId, String cabinClassCode, String from, String to, Integer limit);
}
