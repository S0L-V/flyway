package com.flyway.payment.controller;

import com.flyway.payment.domain.RefundRequest;
import com.flyway.payment.dto.PaymentViewDto;
import com.flyway.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 결제 API 컨트롤러 (REST API)
 *
 * 마이페이지 등 다른 모듈에서 호출하는 API입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;

    /**
     * 결제 정보 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentViewDto> getPayment(@PathVariable String paymentId) {

        PaymentViewDto payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 예약 ID로 결제 정보 조회
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentViewDto> getPaymentByReservation(
            @PathVariable String reservationId) {

        PaymentViewDto payment = paymentService.getPaymentByReservation(reservationId);
        return ResponseEntity.ok(payment);
    }

    /**
     * 환불 요청
     *
     * 마이페이지에서 호출
     * @param paymentId 결제 ID
     * @param request cancelReason, cancelAmount(선택)
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable String paymentId,
            @RequestBody RefundRequest request) {

        log.info("[환불] 요청 - paymentId: {}, reason: {}",
                paymentId, request.getCancelReason());

        try {
            PaymentViewDto result = paymentService.refund(paymentId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[환불] 실패 - paymentId: {}, error: {}", paymentId, e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}