package com.flyway.payment.repository;

import com.flyway.payment.dto.PaymentDto;
import com.flyway.payment.mapper.PaymentQueryMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepositoryImpl implements PaymentQueryRepository {

    private final PaymentQueryMapper paymentQueryMapper;

    @Override
    public Optional<PaymentDto> findLatestPaidByReservationIdAndUserId(String reservationId, String userId) {
        return Optional.ofNullable(
                paymentQueryMapper.findLatestPaidByReservationIdAndUserId(reservationId, userId)
        );
    }
}
