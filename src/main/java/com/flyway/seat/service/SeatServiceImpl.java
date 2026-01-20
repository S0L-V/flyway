package com.flyway.seat.service;

import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.dto.SeatHoldRequest;
import com.flyway.seat.dto.SeatHoldResponse;
import com.flyway.seat.dto.SeatLockRow;
import com.flyway.seat.mapper.SeatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;

    public SeatServiceImpl(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatMap(String flightId) {
        return seatMapper.selectSeatMapByFlightId(flightId);
    }

    @Override
    @Transactional
    public int releaseExpiredHolds() {
        return seatMapper.releaseExpiredHolds();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatMapByReservationSegment(String reservationId, String reservationSegmentId) {
        String flightId = seatMapper.selectFlightIdByReservationSegment(reservationId, reservationSegmentId);
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("해당 구간 예약이 없거나 이 예약에 포함되지 않은 구간입니다.");
        }
        return seatMapper.selectSeatMapByFlightId(flightId);
    }

    @Override
    @Transactional
    public SeatHoldResponse holdSeat(String reservationId, String reservationSegmentId, SeatHoldRequest request) {
        if (request == null
                || request.getPassengerId() == null || request.getPassengerId().isBlank()
                || request.getSeatNo() == null || request.getSeatNo().isBlank()) {
            throw new IllegalArgumentException("승객 id와 좌석 번호는 필수입니다.");
        }

        // reservation 상태 확인
        String status = seatMapper.selectReservationStatus(reservationId);
        if (status == null) {
            throw new IllegalArgumentException("예약이 존재하지 않습니다.");
        }
        if (!"HELD".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("좌석 선택은 HELD 상태에서만 가능합니다.");
        }

        // 만료 확인
        LocalDateTime expiredAt = seatMapper.selectReservationExpiredAt(reservationId);
        if (expiredAt == null) {
            throw new IllegalStateException("예약 만료 시간이 없습니다. expired_at 값을 확인하세요.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (expiredAt.isBefore(now)) {
            throw new IllegalArgumentException("예약이 만료되었습니다.");
        }

        // segment -> flightId
        String flightId = seatMapper.selectFlightIdByReservationSegment(reservationId, reservationSegmentId);
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("해당 구간 예약이 없거나 이 예약에 포함되지 않은 구간입니다.");
        }

        // passenger가 이 reservation 소속인지
        int ok = seatMapper.countPassengerInReservation(reservationId, request.getPassengerId());
        if (ok == 0) {
            throw new IllegalArgumentException("해당 승객이 이 예약에 포함되어 있지 않습니다.");
        }

        // HOLD 만료 = reservation 만료
        LocalDateTime holdExpiresAt = expiredAt;

        // (동시성) aircraft_seat 행을 FOR UPDATE로 먼저 락
        String aircraftSeatId = seatMapper.selectAircraftSeatIdByFlightAndSeatNoForUpdate(flightId, request.getSeatNo());
        if (aircraftSeatId == null || aircraftSeatId.isBlank()) {
            throw new IllegalArgumentException("존재하지 않는 좌석입니다.");
        }

        // 승객이 이미 잡고 있던 좌석 있으면(같은 sid에서) HOLD 해제 후 변경
        String oldFlightSeatId = seatMapper.selectPassengerHeldFlightSeatIdForUpdate(reservationSegmentId, request.getPassengerId());
        if (oldFlightSeatId != null && !oldFlightSeatId.isBlank()) {
            seatMapper.releaseHoldByFlightSeatId(oldFlightSeatId);
        }

        // flight_seat 락 조회 (없으면 insert)
        SeatLockRow row = seatMapper.selectFlightSeatForUpdate(flightId, aircraftSeatId);

        if (row == null) {
            try {
                seatMapper.insertFlightSeatHold(flightId, reservationSegmentId, aircraftSeatId, holdExpiresAt);
            } catch (Exception ignore) {
                // 동시 INSERT 경쟁이면 무시하고 아래에서 다시 락 조회
            }
            row = seatMapper.selectFlightSeatForUpdate(flightId, aircraftSeatId);
            if (row == null) {
                throw new IllegalStateException("좌석 HOLD 처리 중 오류가 발생했습니다.");
            }
        } else {
            // 상태 판단
            if ("BOOKED".equalsIgnoreCase(row.getSeatStatus())) {
                throw new IllegalStateException("이미 예약된 좌석입니다.");
            }

            if ("HOLD".equalsIgnoreCase(row.getSeatStatus())) {
                // 아직 유효한 HOLD면 다른 사람이 점유 중
                if (row.getHoldExpiresAt() != null && row.getHoldExpiresAt().isAfter(now)) {
                    if (row.getHoldReservationSegmentId() == null
                            || !reservationSegmentId.equals(row.getHoldReservationSegmentId())) {
                        throw new IllegalStateException("다른 사용자가 임시 점유 중인 좌석입니다.");
                    }
                    // 같은 segment가 HOLD한 좌석이면 통과(갱신만)
                }
                // 만료된 HOLD면 선점 가능 (아래 update로 HOLD 갱신됨)
            }
        }

        // HOLD 업데이트
        seatMapper.updateFlightSeatHold(row.getFlightSeatId(), reservationSegmentId, holdExpiresAt);

        // 9) passenger_seat upsert (UK(reservation_segment_id, passenger_id) 필요)
        seatMapper.upsertPassengerSeat(reservationSegmentId, request.getPassengerId(), row.getFlightSeatId());

        return SeatHoldResponse.builder()
                .reservationId(reservationId)
                .reservationSegmentId(reservationSegmentId)
                .flightId(flightId)
                .passengerId(request.getPassengerId())
                .flightSeatId(row.getFlightSeatId())
                .seatNo(row.getSeatNo())
                .cabinClassCode(row.getCabinClassCode())
                .seatStatus("HOLD")
                .holdExpiresAt(holdExpiresAt)
                .serverTime(now)
                .build();
    }
}
