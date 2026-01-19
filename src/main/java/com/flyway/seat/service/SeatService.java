package com.flyway.seat.service;

import com.flyway.seat.dto.SeatDTO;
import java.util.List;

public interface SeatService {
    // 항공편별 좌석 맵 조회
    List<SeatDTO> getSeatMap(String flightId);

    // 만료된 HOLD 좌석을 AVAILABLE로 복구 (배치/스케줄러에서 호출)
    int releaseExpiredHolds();
}
