package com.flyway.search.mapper;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.FlightSearchResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlightMapper {
    List<Flight> list(Flight vo);
    List<Airport> airport(Airport vo);
    List<Airline> airline(Airline vo);
    List<FlightSearchResponse> outbound(FlightSearchRequest dto);
    List<FlightSearchResponse> inbound(FlightSearchRequest dto);
    FlightDetailDto details(@Param("cabinClass") String cabinClass, @Param("routeType") String routeType);
}
