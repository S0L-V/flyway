package com.flyway.payment.repository;

import com.flyway.payment.dto.PaymentViewDto;

import java.util.Optional;

/**
 * 결제 Repository 인터페이스
 *
 * Repository 패턴: Mapper를 감싸서 비즈니스 로직과 DB 접근을 분리
 * - Service → Repository → Mapper → DB
 * - 나중에 DB가 바뀌어도 Repository 구현체만 교체하면 됨
 */
public interface PaymentRepository {

    /**
     * 결제 정보 저장
     */
    void insert(PaymentViewDto dto);

    /**
     * 결제 ID로 조회
     */
    Optional<PaymentViewDto> findByPaymentId(String paymentId);

    /**
     * 주문 ID로 조회
     */
    Optional<PaymentViewDto> findByOrderId(String orderId);

    /**
     * 예약 ID로 조회
     */
    Optional<PaymentViewDto> findByReservationId(String reservationId);

    /**
     * 결제 상태 업데이트
     */
    void updateStatus(String paymentId, String status);

    /**
     * 토스 paymentKey 저장
     */
    void updatePaymentKey(String paymentId, String paymentKey);

    void updatePaymentComplete(String paymentId, String paymentKey,String status, String method);
}
