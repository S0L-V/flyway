package com.flyway.search.mapper;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.FlightSearchResponse;
import com.flyway.search.dto.LastPriceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FlightMapper {
    List<Flight> list(Flight vo);
    List<Airport> depAirport(Airport vo);
    List<Airport> arrAirport(@Param("depAirport") String depAirport);
    List<Airline> airline(Airline vo);
    List<FlightSearchResponse> outbound(FlightSearchRequest dto);
    List<FlightSearchResponse> inbound(FlightSearchRequest dto);
    FlightDetailDto details(@Param("cabinClass") String cabinClass, @Param("routeType") String routeType);
    List<LastPriceDto> findPrice(@Param("outFlightId") String outFlightId, @Param("inFlightId") String inFlightId,@Param("cabinClassCode") String cabinClassCode);
    List<Map<String, Object>> selectPriceHistoryPoints(@Param("flightId") String flightId, @Param("cabinClassCode") String cabinClassCode, @Param("from") String from, @Param("to") String to, @Param("limit") Integer limit);

}
