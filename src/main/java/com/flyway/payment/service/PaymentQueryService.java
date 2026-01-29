package com.flyway.payment.service;

import com.flyway.payment.dto.PaymentDto;

public interface PaymentQueryService {

    /**
     * 예약별 결제 조회 (최근 PAID)
     */
    PaymentDto getLatestPaidByReservationId(String reservationId);
}
