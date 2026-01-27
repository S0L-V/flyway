package com.flyway.search.controller;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightDetailDto;
import com.flyway.search.dto.FlightSearchRequest;
import com.flyway.search.dto.LastPriceDto;
import com.flyway.search.dto.SearchResultDto;
import com.flyway.search.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FlightApiController {
    @Autowired
    FlightService service;

    // 항공편 리스트 테스트
    @GetMapping("/api/flights")
    public List<Flight> list(Flight vo) {
        return service.list(vo);
    }

    // 공항 api(검색 옵션)
    @GetMapping("/api/public/depAirports")
    public List<Airport> depAirport(Airport vo) {
        return service.depAirport(vo);
    }

    @GetMapping("/api/public/arrAirports")
    public List<Airport> airport(@RequestParam String depAirport) {
        return service.arrAirport(depAirport);
    }

    @GetMapping("/api/public/airlines")
    public List<Airline> airline(Airline vo) {
        return service.airline(vo);
    }

    // 검색
    @PostMapping("/api/public/flights/search")
    public SearchResultDto search(@RequestBody FlightSearchRequest dto) {
        return service.search(dto);
    }

    // 여정상세
    @GetMapping("/api/public/flights/details")
    public FlightDetailDto details(@RequestParam String cabinClass, @RequestParam String routeType) {
        return service.details(cabinClass, routeType);
    }

    @GetMapping("/api/public/flights/prices")
    public List<LastPriceDto> prices(@RequestParam String outFlightId, @RequestParam String inFlightId, @RequestParam String cabinClassCode) {
        return service.findPrice(outFlightId, inFlightId, cabinClassCode);
    }

}
