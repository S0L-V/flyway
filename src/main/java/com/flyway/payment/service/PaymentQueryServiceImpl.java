package com.flyway.payment.service;

import com.flyway.payment.dto.PaymentDto;
import com.flyway.payment.repository.PaymentQueryRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentQueryRepository paymentQueryRepository;

    @Override
    public PaymentDto getLatestPaidByReservationIdAndUserId(String reservationId, String userId) {
        if (!StringUtils.hasText(reservationId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return paymentQueryRepository.findLatestPaidByReservationIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
