package com.flyway.payment.repository;

import com.flyway.payment.dto.PaymentViewDto;
import com.flyway.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 결제 Repository 구현체
 *
 * @Repository: 스프링이 이 클래스를 Repository 빈으로 등록
 * @RequiredArgsConstructor: final 필드를 받는 생성자 자동 생성 (의존성 주입)
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentMapper paymentMapper;

    @Override
    public void insert(PaymentViewDto dto) {
        paymentMapper.insertPayment(dto);
    }

    @Override
    public Optional<PaymentViewDto> findByPaymentId(String paymentId) {
        return Optional.ofNullable(paymentMapper.selectByPaymentId(paymentId));
    }

    @Override
    public Optional<PaymentViewDto> findByOrderId(String orderId) {
        return Optional.ofNullable(paymentMapper.selectByOrderId(orderId));
    }

    @Override
    public Optional<PaymentViewDto> findByReservationId(String reservationId) {
        return Optional.ofNullable(paymentMapper.selectByReservationId(reservationId));
    }

    @Override
    public void updateStatus(String paymentId, String status) {
        paymentMapper.updateStatus(paymentId, status);
    }

    @Override
    public void updatePaymentKey(String paymentId, String paymentKey) {
        paymentMapper.updatePaymentKey(paymentId, paymentKey);
    }

    @Override
    public void updatePaymentComplete(String paymentId, String paymentKey, String status, String method) {
        paymentMapper.updatePaymentComplete(paymentId,paymentKey, status, method);
    }
}
