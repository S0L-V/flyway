package com.flyway.search.mapper;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.FlightSearchResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlightMapper {
    List<Flight> list(Flight vo);
    List<Airport> airport(Airport vo);
    List<FlightSearchResponse> outbound(FlightSearchRequest dto);
    List<FlightSearchResponse> inbound(FlightSearchRequest dto);
}
