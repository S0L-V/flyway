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
    public PaymentDto getLatestPaidByReservationId(String reservationId) {
        if (!StringUtils.hasText(reservationId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return paymentQueryRepository.findLatestPaidByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_LIST_FETCH_FAILED));
    }
}
