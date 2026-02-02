package com.flyway.seat.controller;
import com.flyway.seat.dto.*;
import com.flyway.seat.service.SeatService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")  // ★ /api/public 제거 → 인증 필수
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 항공편별 좌석 맵 조회 (읽기 전용 - 인증 필요)
     */
    @GetMapping("/flights/{flightId}")
    public ApiResponse<List<SeatDTO>> getSeatMap(
            @PathVariable String flightId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 읽기 전용이지만 인증된 사용자만 허용
        return ApiResponse.success(seatService.getSeatMap(flightId));
    }

    /**
     * 예약(rid) + 구간(sid) 기반 좌석 맵 조회
     */
    @GetMapping("/reservations/{rid}/segments/{sid}")
    public ApiResponse<List<SeatDTO>> getSeatMapByReservationSegment(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        //  본인 예약인지 검증
        seatService.validateReservationOwner(reservationId, user.getUserId());

        return ApiResponse.success(
                seatService.getSeatMapByReservationSegment(reservationId, reservationSegmentId)
        );
    }

    /**
     * 좌석 HOLD (임시 점유)
     */
    @PostMapping("/reservations/{rid}/segments/{sid}/hold")
    public ApiResponse<SeatHoldResponse> holdSeat(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId,
            @RequestBody SeatHoldRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 본인 예약인지 검증
        seatService.validateReservationOwner(reservationId, user.getUserId());

        return ApiResponse.success(
                seatService.holdSeat(reservationId, reservationSegmentId, request)
        );
    }

    /**
     * 좌석 HOLD 해제
     */
    @DeleteMapping("/reservations/{rid}/segments/{sid}/hold/{passengerId}")
    public ApiResponse<SeatReleaseResponse> releaseSeat(
            @PathVariable("rid") String reservationId,
            @PathVariable("sid") String reservationSegmentId,
            @PathVariable("passengerId") String passengerId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        // 본인 예약인지 검증
        seatService.validateReservationOwner(reservationId, user.getUserId());

        return ApiResponse.success(
                seatService.releaseSeat(reservationId, reservationSegmentId, passengerId)
        );
    }
}