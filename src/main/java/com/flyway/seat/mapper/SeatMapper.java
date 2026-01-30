package com.flyway.seat.mapper;

import com.flyway.seat.dto.PassengerDTO;
import com.flyway.seat.dto.SeatDTO;
import com.flyway.seat.dto.SeatLockRow;
import com.flyway.seat.dto.SegmentCardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeatMapper {

    // 배치 - 만료된 HOLD 복구
    int releaseExpiredHolds();

    // 배치 - 만료된 HOLD에 연결된 passenger_seat 정리
    int deletePassengerSeatForExpiredHolds();

    // 좌석맵 조회
    List<SeatDTO> selectSeatMapByFlightId(@Param("flightId") String flightId);

    // reservation(rid) + segment(sid) -> flight_id
    String selectFlightIdByReservationSegment(
            @Param("reservationId") String reservationId,
            @Param("reservationSegmentId") String reservationSegmentId
    );

    // reservation 상태 조회 (HOLD/CONFIRMED/EXPIRED)
    String selectReservationStatus(@Param("reservationId") String reservationId);

    // reservation 만료 시각 조회 (expired_at)
    LocalDateTime selectReservationExpiredAt(@Param("reservationId") String reservationId);

    // passenger가 reservation에 속하는지 체크
    int countPassengerInReservation(
            @Param("reservationId") String reservationId,
            @Param("passengerId") String passengerId
    );

    // flight_id + seatNo로 aircraft_seat_id 조회 (좌석번호 유효성 체크용) + FOR UPDATE (xml에서 FOR UPDATE)
    String selectAircraftSeatIdByFlightAndSeatNoForUpdate(
            @Param("flightId") String flightId,
            @Param("seatNo") String seatNo
    );

    // 승객이 이미 구간에서 잡고 있는 좌석이 있으면 그 flight_seat_id 반환 (행 락)
    String selectPassengerHoldFlightSeatIdForUpdate(
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("passengerId") String passengerId
    );

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
    int releaseHoldByFlightSeatId(
            @Param("flightSeatId") String flightSeatId,
            @Param("reservationSegmentId") String reservationSegmentId
    );

    // passenger_seat upsert
    int upsertPassengerSeat(
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("passengerId") String passengerId,
            @Param("flightSeatId") String flightSeatId
    );

    // 좌석 HOLD 해제용: passenger_seat 삭제
    int deletePassengerSeatBySegmentAndPassenger(
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("passengerId") String passengerId
    );
    // 좌석 팝업에서 HOLD 테스트용 해당 승객 1명 passenger_id 조회
    String selectFirstPassengerIdByReservationId(@Param("reservationId")
                                                 String reservationId);

    // reservationId 기준 구간 카드용 정보 조회 (출발/도착/출발시간)
    List<SegmentCardDTO> selectSegmentCardsByReservationId(@Param("reservationId") String reservationId);

    // 예약(reservationId) 기준 탑승자 목록 조회 (좌석 팝업 다인원 탭용)
    List<PassengerDTO> selectPassengersByReservationId(@Param("reservationId") String reservationId);

    // 결제 확정 (reservationId 기준 유효한 HOLD 좌석 수 카운트 함)
    int countActiveHoldSeatsByReservation(
            @Param("reservationId") String reservationId,
            @Param("now") LocalDateTime now
    );

    // reservationId가 잡고 있던 HOLD 좌석들을 BOOKED로 확정
    int bookHoldSeatsByReservation(
            @Param("reservationId") String reservationId,
            @Param("now") LocalDateTime now
    );

    int releaseBookedSeatsByReservation(
            @Param("reservationId") String reservationId);

}
