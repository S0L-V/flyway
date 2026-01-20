package com.flyway.seat.service;

import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.dto.SeatHoldRequest;
import com.flyway.seat.dto.SeatHoldResponse;

import java.util.List;

public interface SeatService {
    // 항공편별 좌석 맵 조회
    List<SeatDTO> getSeatMap(String flightId);

    // 예약 + 구간 기준 좌석 조회
    List<SeatDTO> getSeatMapByReservationSegment(
            String reservationId,
            String reservationSegmentId
    );

    // 만료된 HOLD 좌석을 AVAILABLE로 복구 (배치/스케줄러에서 호출)
    int releaseExpiredHolds();

    SeatHoldResponse holdSeat(String reservationId, String reservationSegmentId, SeatHoldRequest request);

}
