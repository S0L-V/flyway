package com.flyway.search.repository;

import com.flyway.search.domain.Airport;
import com.flyway.search.domain.Flight;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.FlightSearchResponse;
import com.flyway.search.mapper.FlightMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FlightRepositoryImpl implements FlightRepository {
    private final FlightMapper mapper; // Mapper는 여기서만 씁니다!

    public List<Flight> findAll(Flight vo) {
        return mapper.list(vo);
    }

    public List<Airport> findAirports(Airport vo) {
        return mapper.airport(vo);
    }

    public List<FlightSearchResponse> findOutboundFlights(FlightSearchRequest dto) {
        return mapper.outbound(dto);
    }

    public List<FlightSearchResponse> findInboundFlights(FlightSearchRequest dto) {
        return mapper.inbound(dto);
    }
}
