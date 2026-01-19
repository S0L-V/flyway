package com.flyway.seat.controller;

import com.flyway.template.common.ApiResponse;
import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.service.SeatService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    // 항공편별 좌석 맵 조회
    @GetMapping("/api/flights/{flightId}/seats")
    public ApiResponse<List<SeatDTO>> getSeatMap(@PathVariable String flightId){
        return ApiResponse.<List<SeatDTO>>success(seatService.getSeatMap(flightId));

    }
}
