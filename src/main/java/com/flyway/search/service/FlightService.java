package com.flyway.search.service;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightSearchRequestDTO;
import com.flyway.search.dto.SearchResultDTO;

import java.util.List;

public interface FlightService {
    List<FlightVO> list(FlightVO vo);
    List<AirportVO> airport(AirportVO vo);
    SearchResultDTO search(FlightSearchRequestDTO dto);
}
