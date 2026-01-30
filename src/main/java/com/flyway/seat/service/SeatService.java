package com.flyway.seat.service;

import com.flyway.seat.dto.*;

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

    // 좌석 HOLD
    SeatHoldResponse holdSeat(
            String reservationId,
            String reservationSegmentId,
            SeatHoldRequest request
    );

    // 좌석 RELEASE
    SeatReleaseResponse releaseSeat(
            String reservationId,
            String reservationSegmentId,
            String passengerId
    );

    String findFirstPassengerId(String reservationId);

    // 예약(reservationId) 기준 구간 카드용 정보 조회 (출발/도착/출발시간)
    List<SegmentCardDTO> getSegmentCards(String reservationId);

    // 예약(reservationId) 기준 탑승자 목록(다인원 탭용)
    List<PassengerDTO> findPassengers(String reservationId);

    void bookHoldSeats(String reservationId);

    void releaseBookedSeats(String reservationId);

}
