package com.flyway.seat.controller;

import com.flyway.seat.dto.*;
import com.flyway.seat.service.SeatService;
import com.flyway.template.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    // 항공편별 좌석 맵 조회
    @GetMapping("/api/public/flights/{flightId}/seats")
    public ApiResponse<List<SeatDTO>> getSeatMap(@PathVariable String flightId) {
        return ApiResponse.success(seatService.getSeatMap(flightId));
    }

    // 예약(rid) + 구간(sid) 기반 좌석 맵 조회
    @GetMapping("/api/public/reservations/{rid}/segments/{sid}/seats")
    public ApiResponse<List<SeatDTO>> getSeatMapByReservationSegment(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId
    ) {
        return ApiResponse.success(
                seatService.getSeatMapByReservationSegment(
                        reservationId,
                        reservationSegmentId
                )
        );
    }

    // 좌석 HOLD
    @PostMapping("/api/public/reservations/{rid}/segments/{sid}/seats/hold")
    public ApiResponse<SeatHoldResponse> holdSeat(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId,
            @RequestBody SeatHoldRequest request
    ) {
        return ApiResponse.success(
                seatService.holdSeat(
                        reservationId,
                        reservationSegmentId,
                        request
                )
        );
    }

    @DeleteMapping("/api/public/reservations/{rid}/segments/{sid}/seats/hold/{passengerId}")
    public ApiResponse<SeatReleaseResponse> releaseSeat(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId,
            @PathVariable("passengerId") String passengerId
    ) {
        return ApiResponse.success(
                seatService.releaseSeat(
                        reservationId,
                        reservationSegmentId,
                        passengerId
                )
        );
    }
}
