package com.flyway.seat.mapper;

import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.dto.SeatLockRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeatMapper {

    // 배치 - 만료된 HOLD 복구
    int releaseExpiredHolds();

    // 좌석맵 조회
    List<SeatDTO> selectSeatMapByFlightId(@Param("flightId") String flightId);

    // reservation(rid) + segment(sid) -> flight_id
    String selectFlightIdByReservationSegment(
            @Param("reservationId") String reservationId,
            @Param("reservationSegmentId") String reservationSegmentId
    );

    // HOLD 구현에 필요한 조회/검증

    // reservation 상태 조회 (HELD/CONFIRMED/EXPIRED)
    String selectReservationStatus(@Param("reservationId") String reservationId);

    // reservation 만료 시각 조회 (expired_at)
    LocalDateTime selectReservationExpiredAt(@Param("reservationId") String reservationId);

    // passenger가 reservation에 속하는지 체크
    int countPassengerInReservation(
            @Param("reservationId") String reservationId,
            @Param("passengerId") String passengerId
    );

    // flight_id + seatNo로 aircraft_seat_id 조회 (좌석번호 유효성 체크용)
    String selectAircraftSeatIdByFlightAndSeatNoForUpdate(
            @Param("flightId") String flightId,
            @Param("seatNo") String seatNo
    );

    // 승객이 이미 구간에서 잡고 있는 좌석이 있으면 그 flight_seat_id 반환 (행 락)
    String selectPassengerHeldFlightSeatIdForUpdate(
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("passengerId") String passengerId
    );

    // flight_seat 락/생성/업데이트

    // flight_seat 행을 락 걸고 조회 (없으면 null)
    SeatLockRow selectFlightSeatForUpdate(
            @Param("flightId") String flightId,
            @Param("aircraftSeatId") String aircraftSeatId
    );

    // flight_seat가 없으면 HOLD row를 생성 (UNIQUE(flight_id, aircraft_seat_id) 기반)
    int insertFlightSeatHold(
            @Param("flightId") String flightId,
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("aircraftSeatId") String aircraftSeatId,
            @Param("holdExpiresAt") LocalDateTime holdExpiresAt
    );

    // flight_seat을 HOLD로 갱신
    int updateFlightSeatHold(
            @Param("flightSeatId") String flightSeatId,
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("holdExpiresAt") LocalDateTime holdExpiresAt
    );

    // 기존 HOLD 좌석 해제 (HOLD인 경우만 AVAILABLE로)
    int releaseHoldByFlightSeatId(@Param("flightSeatId") String flightSeatId);

    // passenger_seat upsert
    int upsertPassengerSeat(
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("passengerId") String passengerId,
            @Param("flightSeatId") String flightSeatId
    );
}
