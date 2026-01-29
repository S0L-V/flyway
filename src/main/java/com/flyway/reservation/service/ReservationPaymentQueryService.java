package com.flyway.reservation.service;

import com.flyway.reservation.dto.ReservationPaymentResponseDto;

public interface ReservationPaymentQueryService {

    /**
     * 예약 결제 상세 조회
     */
    ReservationPaymentResponseDto getReservationPaymentDetail(String reservationId);
}
