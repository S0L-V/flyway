package com.flyway.reservation.repository;

import com.flyway.reservation.dto.ReservationPaymentDetailDto;

import java.util.Optional;

public interface ReservationPaymentQueryRepository {

    /**
     * 예약 결제 상세 조회
     */
    Optional<ReservationPaymentDetailDto> findReservationPaymentDetail(String reservationId);
}
