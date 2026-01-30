package com.flyway.seat.service;

import com.flyway.seat.dto.*;
import com.flyway.seat.mapper.SeatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;

    public SeatServiceImpl(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @Override
    public List<SeatDTO> getSeatMap(String flightId) {
        return seatMapper.selectSeatMapByFlightId(flightId);
    }

    @Override
    @Transactional
    public int releaseExpiredHolds() {
        // 먼저 passenger_seat 정리 (만료 HOLD에 달린 참조 제거)
        seatMapper.deletePassengerSeatForExpiredHolds();

        // 그 다음 flight_seat을 AVAILABLE로 복구
        return seatMapper.releaseExpiredHolds();
    }

    @Override
    public List<SeatDTO> getSeatMapByReservationSegment(String reservationId, String reservationSegmentId) {
        String flightId = seatMapper.selectFlightIdByReservationSegment(reservationId, reservationSegmentId);
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("해당 구간 예약이 없거나 이 예약에 포함되지 않은 구간입니다.");
        }
        return seatMapper.selectSeatMapByFlightId(flightId);
    }

    @Override
    public String findFirstPassengerId(String reservationId) {
        String passengerId = seatMapper.selectFirstPassengerIdByReservationId(reservationId);
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("해당 예약에 승객이 없습니다.");
        }
        return passengerId;
    }

    // 예약(reservationId) 기준 구간 카드용 정보 조회 (출발/도착/출발시간)
    @Override
    public List<SegmentCardDTO> getSegmentCards(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("reservationId는 필수입니다.");
        }
        return seatMapper.selectSegmentCardsByReservationId(reservationId);
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
            throw new IllegalArgumentException("좌석 선택은 예약 진행 중에만 가능합니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        // 만료 확인 중복 제거 - 공통 메서드로 검증 + expiredAt을 받아서 holdExpiresAt에 사용
        LocalDateTime expiredAt = getExpiredAtOrThrow(reservationId, now);

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
        String oldFlightSeatId = seatMapper.selectPassengerHoldFlightSeatIdForUpdate(
                reservationSegmentId,
                request.getPassengerId()
        );

        // flight_seat 락 조회 (없으면 insert)
        SeatLockRow row = seatMapper.selectFlightSeatForUpdate(flightId, aircraftSeatId);

        if (row == null) {
            try {
                seatMapper.insertFlightSeatHold(flightId, reservationSegmentId, aircraftSeatId, holdExpiresAt);
            } catch (Exception ignore) {
                // 동시 INSERT 경쟁이면 무시하고 아래에서 다시 락 조회
            }

            row = seatMapper.selectFlightSeatForUpdate(flightId, aircraftSeatId);

            if (row == null || row.getFlightSeatId() == null || row.getFlightSeatId().isBlank()) {
                throw new IllegalStateException("좌석을 선택하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
        }

        // 상태 판단 (row 존재 시 항상 검사)
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

        // HOLD 업데이트
        int updated = seatMapper.updateFlightSeatHold(row.getFlightSeatId(), reservationSegmentId, holdExpiresAt);
        if (updated == 0) {
            throw new IllegalStateException("좌석 HOLD 처리 중 동시성 충돌이 발생했습니다.");
        }

        // passenger_seat upsert (UK(reservation_segment_id, passenger_id) 필요)
        seatMapper.upsertPassengerSeat(reservationSegmentId, request.getPassengerId(), row.getFlightSeatId());

        // 새 좌석 upsert가 끝난 뒤에 기존 좌석이 다른 좌석이면 해제
        if (oldFlightSeatId != null && !oldFlightSeatId.isBlank()
                && !oldFlightSeatId.equals(row.getFlightSeatId())) {
            seatMapper.releaseHoldByFlightSeatId(oldFlightSeatId, reservationSegmentId);
        }

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

    @Override
    @Transactional
    public SeatReleaseResponse releaseSeat(
            String reservationId,
            String reservationSegmentId,
            String passengerId
    ) {
        // 입력값 검증
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("승객 id는 필수입니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        // reservation 상태 확인
        String status = seatMapper.selectReservationStatus(reservationId);
        if (status == null) {
            throw new IllegalArgumentException("예약이 존재하지 않습니다.");
        }
        if (!"HELD".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("좌석 해제는 HOLD 상태에서만 가능합니다.");
        }

        // 예약 만료 체크 중복 제거 - 공통 메서드 호출(리턴값이 필요 없으면 호출만)
        getExpiredAtOrThrow(reservationId, now);

        // segment -> flightId
        String flightId = seatMapper.selectFlightIdByReservationSegment(
                reservationId,
                reservationSegmentId
        );
        if (flightId == null || flightId.isBlank()) {
            throw new IllegalArgumentException("해당 구간 예약이 없거나 이 예약에 포함되지 않은 구간입니다.");
        }

        // passenger가 이 reservation 소속인지
        int ok = seatMapper.countPassengerInReservation(reservationId, passengerId);
        if (ok == 0) {
            throw new IllegalArgumentException("해당 승객이 이 예약에 포함되어 있지 않습니다.");
        }

        // 승객이 구간에서 잡고 있는 flight_seat_id 조회 (FOR UPDATE로 잠금)
        String flightSeatId = seatMapper.selectPassengerHoldFlightSeatIdForUpdate(
                reservationSegmentId,
                passengerId
        );

        // 잡은 좌석이 없으면 정상 응답
        if (flightSeatId == null || flightSeatId.isBlank()) {
            return SeatReleaseResponse.builder()
                    .reservationId(reservationId)
                    .reservationSegmentId(reservationSegmentId)
                    .flightId(flightId)
                    .passengerId(passengerId)
                    .releasedFlightSeatId(null)
                    .seatStatus("AVAILABLE")
                    .serverTime(now)
                    .build();
        }

        // passenger_seat 삭제 (승객-좌석 연결 끊기)
        seatMapper.deletePassengerSeatBySegmentAndPassenger(reservationSegmentId, passengerId);

        // flight_seat을 AVAILABLE로 복구
        seatMapper.releaseHoldByFlightSeatId(flightSeatId, reservationSegmentId);

        return SeatReleaseResponse.builder()
                .reservationId(reservationId)
                .reservationSegmentId(reservationSegmentId)
                .flightId(flightId)
                .passengerId(passengerId)
                .releasedFlightSeatId(flightSeatId)
                .seatStatus("AVAILABLE")
                .serverTime(now)
                .build();
    }

    @Override
    public List<PassengerDTO> findPassengers(String reservationId) {
        return seatMapper.selectPassengersByReservationId(reservationId);
    }


    // 중복 코드 제거용
    private LocalDateTime getExpiredAtOrThrow(String reservationId, LocalDateTime now) {
        LocalDateTime expiredAt = seatMapper.selectReservationExpiredAt(reservationId);
        if (expiredAt == null) {
            throw new IllegalStateException("예약 만료 시간이 없습니다. expired_at 값을 확인하세요.");
        }
        if (expiredAt.isBefore(now)) {
            throw new IllegalArgumentException("예약이 만료되었습니다.");
        }
        return expiredAt;
    }
}
