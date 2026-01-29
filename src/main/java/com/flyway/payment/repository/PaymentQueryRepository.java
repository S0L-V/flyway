package com.flyway.payment.repository;

import com.flyway.payment.dto.PaymentDto;
import java.util.Optional;

public interface PaymentQueryRepository {

    /**
     * 예약별 결제 조회 (최근 PAID)
     */
    Optional<PaymentDto> findLatestPaidByReservationIdAndUserId(String reservationId, String userId);
}
