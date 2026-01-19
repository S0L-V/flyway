package com.flyway.seat.mapper;

import com.flyway.seat.dto.SeatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeatMapper {
    // 만료된 HOLD 좌석을 AVAILABLE로 되돌림
    int releaseExpiredHolds();

    // 항공편별 좌석 맵 조회
    List<SeatDTO> selectSeatMapByFlightId(@Param("flightId") String flightId);
}
