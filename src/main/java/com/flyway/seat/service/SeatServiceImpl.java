package com.flyway.seat.service;

import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.mapper.SeatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;

    public SeatServiceImpl(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    /**
     * 좌석 맵 조회는 조회만 수행 (UPDATE 없음)
     */
    @Override
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatMap(String flightId) {
        return seatMapper.selectSeatMapByFlightId(flightId);
    }

    /**
     * 만료된 HOLD 좌석 복구는 배치/스케줄러에서 호출
     * 트랜잭션은 Service 계층에서 관리
     */
    @Override
    @Transactional
    public int releaseExpiredHolds() {
        return seatMapper.releaseExpiredHolds();
    }
}
