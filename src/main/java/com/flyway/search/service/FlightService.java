package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.SearchResultDto;

import java.util.List;

public interface FlightService {
    List<Flight> list(Flight vo);
    List<Airport> airport(Airport vo);
    SearchResultDto search(FlightSearchRequest dto);
}
