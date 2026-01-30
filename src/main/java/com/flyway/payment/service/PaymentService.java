package com.flyway.payment.service;
import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.service.AdminNotificationService;
import com.flyway.payment.domain.*;
import com.flyway.payment.dto.PaymentViewDto;
import com.flyway.payment.client.TossPaymentsClient;
import com.flyway.payment.mapper.RefundMapper;
import com.flyway.payment.repository.PaymentRepository;
import com.flyway.reservation.dto.ReservationCoreView;
import com.flyway.reservation.dto.ReservationSegmentView;
import com.flyway.reservation.repository.ReservationBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flyway.reservation.repository.PassengerServiceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.flyway.payment.dto.RefundSegmentDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationBookingRepository reservationBookingRepository;
    private final TossPaymentsClient tossClient;
    private final PassengerServiceRepository passengerServiceRepository;
    private final RefundMapper refundMapper;
    private final AdminNotificationService adminNotificationService;

    /**
     * 결제 처리 메인 로직 (개선된 3단계 설계)
     */
    public PaymentViewDto processPayment(PaymentConfirmRequest request, String userId) {
        // 1단계: 결제 준비 (짧은 트랜잭션)
        PaymentViewDto payment = preparePayment(request, userId);

        try {
            // 2단계: 토스 API 호출 (트랜잭션 밖 - DB 커넥션 점유 안함)
            TossPaymentResponse tossResponse = tossClient.confirmPayment(request);

            // 3단계: 결제 성공 처리 (짧은 트랜잭션)
            return completePayment(payment.getPaymentId(), tossResponse);
        } catch (Exception e) {
            // 3단계: 결제 실패 처리 (짧은 트랜잭션)
            failPayment(payment.getPaymentId(), payment.getReservationId(), e.getMessage());
            throw new RuntimeException("결제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 1단계: 결제 준비 - 예약 검증 및 PENDING 상태로 결제 생성
     */
    @Transactional
    public PaymentViewDto preparePayment(PaymentConfirmRequest request, String userId) {
        String reservationId = extractReservationId(request.getOrderId());

        // 예약 row 잠금 (동시 결제 방지)
        ReservationCoreView reservation = reservationBookingRepository.lockReservationForUpdate(reservationId);

        // 예약 검증
        validateReservation(reservation, userId);

        // 금액 계산 및 검증
        long calculatedAmount = calculateTotalAmount(reservationId);
        if (calculatedAmount != request.getAmount()) {
            throw new RuntimeException("결제 금액 불일치: 예상=" + calculatedAmount + ", 요청=" +
                request.getAmount());
        }

        // 예약 상태 변경: HELD → PAYING
        reservationBookingRepository.updateReservationStatus(reservationId, "PAYING");

        // 결제 정보 INSERT (PENDING 상태)
        PaymentViewDto payment = PaymentViewDto.builder()
            .paymentId(UUID.randomUUID().toString())
            .reservationId(reservationId)
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .method("PENDING")
            .status(PaymentStatus.PENDING.name())
            .createdAt(LocalDateTime.now())
            .build();

        paymentRepository.insert(payment);

        return payment;
    }

    /**
     * 3단계 성공: 결제 완료 처리
     */
    @Transactional
    public PaymentViewDto completePayment(String paymentId, TossPaymentResponse tossResponse) {
        // 결제 정보 업데이트 (4개 인자, 순서: paymentId, paymentKey, status, method)
        paymentRepository.updatePaymentComplete(
            paymentId,
            tossResponse.getPaymentKey(),
            PaymentStatus.PAID.name(),
            tossResponse.getMethod()
        );

        // 예약 상태 변경: PAYING → CONFIRMED
        PaymentViewDto payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));
        String reservationId = payment.getReservationId();
        reservationBookingRepository.updateReservationStatus(reservationId, "CONFIRMED");

        // [알림] 신규 예약 발생 알림 생성
        AdminNotificationDto notification = AdminNotificationDto.builder()
            .notificationType("NEW_RESERVATION")
            .title("신규 예약 발생")
            .message("새로운 예약이 확정되었습니다.")
            .relatedResourceType("RESERVATION")
            .relatedResourceId(reservationId)
            .priority("NORMAL")
            .build();
        adminNotificationService.createAndBroadcastNotification(notification);

        return paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));
    }

    /**
     * 3단계 실패: 결제 실패 처리
     */
    @Transactional
    public void failPayment(String paymentId, String reservationId, String failureReason) {
        // 결제 상태 → FAILED
        paymentRepository.updateStatus(paymentId, PaymentStatus.FAILED.name());

        // 예약 상태 → HELD (원복)
        reservationBookingRepository.updateReservationStatus(reservationId, "HELD");

        // [알림] 결제 실패 알림 생성
        AdminNotificationDto notification = AdminNotificationDto.builder()
            .notificationType("PAYMENT_FAILED")
            .title("결제 실패 발생")
            .message("결제 실패가 발생했습니다: " + failureReason)
            .relatedResourceType("RESERVATION")
            .relatedResourceId(reservationId)
            .priority("HIGH")
            .build();
        adminNotificationService.createAndBroadcastNotification(notification);
    }

    /**
     * 환불 처리
     */
    public PaymentViewDto processRefund(String paymentId, RefundRequest request) {
        PaymentViewDto payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

        if (!PaymentStatus.PAID.name().equals(payment.getStatus())) {
            throw new RuntimeException("환불 가능한 상태가 아닙니다: " + payment.getStatus());
        }

        // 토스 환불 API 호출
        request.setPaymentKey(payment.getPaymentKey());
        TossPaymentResponse tossResponse = tossClient.cancelPayment(request);

        // 결제 상태 업데이트
        String newStatus = "CANCELED".equals(tossResponse.getStatus())
            ? PaymentStatus.CANCELLED.name()
            : payment.getStatus();
        paymentRepository.updateStatus(paymentId, newStatus);

        // 예약 상태 → CANCELLED
        String reservationId = payment.getReservationId();
        reservationBookingRepository.updateReservationStatus(payment.getReservationId(), "CANCELLED");
        // 잔여석 복구
        int passengerCount = refundMapper.selectPassengerCountByReservationId(reservationId);
        List<RefundSegmentDto> segments = refundMapper.selectSegmentsByReservationId(reservationId);

        for (RefundSegmentDto segment : segments) {
            refundMapper.incrementSeat(segment.getFlightId(), segment.getCabinClass(), passengerCount);
        }

        // refund 테이블 INSERT
        refundMapper.insertRefund(
            UUID.randomUUID().toString(),
            reservationId,
            paymentId,
            "DEFAULT_RF_ID",  // TODO: 실제 refund_policy 조회 후 설정
            payment.getAmount(),
            payment.getAmount(),
            request.getCancelReason(),
            null
        );

        // [알림] 환불 완료 알림 생성
        AdminNotificationDto notification = AdminNotificationDto.builder()
            .notificationType("REFUND_COMPLETED")
            .title("환불 처리 완료")
            .message("사용자 요청에 의해 환불이 완료되었습니다.")
            .relatedResourceType("RESERVATION")
            .relatedResourceId(reservationId)
            .priority("NORMAL")
            .build();
        adminNotificationService.createAndBroadcastNotification(notification);

        return paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));
    }

    /**
     * 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentViewDto findByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId).orElse(null);
    }

    @Transactional(readOnly = true)
    public PaymentViewDto findByReservationId(String reservationId) {
        return paymentRepository.findByReservationId(reservationId).orElse(null);
    }
    //환불에서 조회
    @Transactional(readOnly = true)
    public List<PaymentViewDto> findPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }


    /**
     * 결제 조회 (Controller용)
     */
    public PaymentViewDto getPayment(String paymentId) {
        return findByPaymentId(paymentId);
    }

    /**
     * 예약 ID로 결제 조회 (Controller용)
     */
    public PaymentViewDto getPaymentByReservation(String reservationId) {
        return findByReservationId(reservationId);
    }

    /**
     * 환불 (Controller용)
     */
    public PaymentViewDto refund(String paymentId, RefundRequest request) {
        return processRefund(paymentId, request);
    }

    /**
     * 주문 ID 생성
     */
    public String generateOrderId(String reservationId) {
        return "ORDER_" + reservationId + "_" + System.currentTimeMillis();
    }



    /**
     * orderId에서 reservationId 추출
     * orderId 형식: "ORDER_{reservationId}_{timestamp}"
     */
    private String extractReservationId(String orderId) {
        if (orderId == null || !orderId.startsWith("ORDER_")) {
            throw new RuntimeException("잘못된 주문 ID 형식: " + orderId);
        }
        String[] parts = orderId.split("_");
        if (parts.length < 2) {
            throw new RuntimeException("주문 ID에서 예약 ID를 추출할 수 없습니다: " + orderId);
        }
        return parts[1];
    }

    /**
     * 예약 유효성 검증
     */
    private void validateReservation(ReservationCoreView reservation, String userId) {
        if (reservation == null) {
            throw new RuntimeException("예약 정보를 찾을 수 없습니다");
        }

        // 본인 예약인지 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 예약만 결제할 수 있습니다");
        }

        // 예약 상태 확인 (HELD 상태만 결제 가능)
        if (!"HELD".equals(reservation.getStatus())) {
            throw new RuntimeException("결제 가능한 상태가 아닙니다: " + reservation.getStatus());
        }

        // 만료 시간 확인
        if (reservation.getExpiredAt() != null &&
            reservation.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("예약이 만료되었습니다");
        }
    }

    /**
     * 총 결제 금액 계산 (구간별 snapPrice 합계)
     */
    private long calculateTotalAmount(String reservationId) {
        List<ReservationSegmentView> segments = reservationBookingRepository.findSegments(reservationId);

        if (segments == null || segments.isEmpty()) {
            throw new RuntimeException("예약 구간 정보가 없습니다: " + reservationId);
        }

        long flightTotal = segments.stream()
            .mapToLong(seg -> seg.getSnapPrice() != null ? seg.getSnapPrice() : 0L)
            .sum();

        // 부가서비스 금액 (수하물, 기내식)
        Long serviceTotal = passengerServiceRepository.findServiceTotal(reservationId);

        return flightTotal + (serviceTotal != null ? serviceTotal : 0L);
    }
}