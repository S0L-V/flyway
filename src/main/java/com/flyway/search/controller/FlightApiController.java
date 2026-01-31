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
    public List<LastPriceDto> prices(@RequestParam String outFlightId, @RequestParam(required = false) String inFlightId, @RequestParam String cabinClassCode) {
        return service.findPrice(outFlightId, inFlightId, cabinClassCode);
    }

    @GetMapping("/api/public/flights/price-history")
    public java.util.Map<String, Object> priceHistory(
            @RequestParam String flightId,
            @RequestParam String cabinClassCode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit
    ) {
        int safeLimit = (limit == null || limit <= 0) ? 2000 : Math.min(limit, 5000);

        List<java.util.Map<String, Object>> points =
                service.findPriceHistory(flightId, cabinClassCode, from, to, safeLimit);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("flightId", flightId);
        res.put("cabinClassCode", cabinClassCode);
        res.put("points", points);
        return res;
    }


}
