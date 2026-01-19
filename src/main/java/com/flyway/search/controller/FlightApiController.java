package com.flyway.search.controller;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightSearchRequestDTO;
import com.flyway.search.dto.SearchResultDTO;
import com.flyway.search.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FlightApiController {
    @Autowired
    FlightService service;

    // 항공편 리스트 테스트
    @GetMapping("/api/flights")
    public List<FlightVO> list(FlightVO vo) {
        return service.list(vo);
    }

    // 공항 api(검색 옵션)
    @GetMapping("/api/public/airports")
    public List<AirportVO> airport(AirportVO vo) {
        return service.airport(vo);
    }

    // 검색
    @PostMapping("/api/public/flights/search")
    public SearchResultDTO search(@RequestBody FlightSearchRequestDTO dto) {
        return service.search(dto);
    }

}
