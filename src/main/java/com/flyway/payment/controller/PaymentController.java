package com.flyway.payment.controller;

import com.flyway.payment.config.TossPaymentsConfig;
import com.flyway.payment.domain.PaymentConfirmRequest;
import com.flyway.payment.dto.PaymentViewDto;
import com.flyway.payment.service.PaymentService;
import com.flyway.reservation.dto.ReservationSegmentView;
import com.flyway.reservation.repository.PassengerServiceRepository;
import com.flyway.reservation.repository.ReservationBookingRepository;
import com.flyway.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 컨트롤러 (웹 페이지용)
 *
 * [엔드포인트]
 * GET  /payments/{reservationId}  - 결제 페이지
 * GET  /payments/success          - 결제 성공 콜백 (토스 → 우리 서버)
 * GET  /payments/fail             - 결제 실패 콜백
 * GET  /payments/complete         - 결제 완료 페이지
 */
@Slf4j
@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TossPaymentsConfig tossConfig;
    private final ReservationBookingRepository reservationBookingRepository;
    private final PassengerServiceRepository passengerServiceRepository;
    // private final ReservationBookingService bookingService;  // 예약 정보 조회용

    /**
     * 결제 페이지
     *
     * booking.jsp에서 "결제하기" 클릭 시 이동
     * 토스 SDK를 로드하고 결제창을 호출합니다.
     */
    @GetMapping("/{reservationId}")
    public String paymentPage(@PathVariable String reservationId,
                              @AuthenticationPrincipal CustomUserDetails user,
                              Model model) {

        log.info("[결제페이지] 진입 - reservationId: {}, userId: {}",
                reservationId, user.getUserId());

        // 실제 금액 계산
        List<ReservationSegmentView> segments = reservationBookingRepository.findSegments(reservationId);
        long flightTotal = segments.stream()
                .mapToLong(seg -> seg.getSnapPrice() != null ? seg.getSnapPrice() : 0L)
                .sum();

        // 부가서비스 금액
        Long serviceTotal = passengerServiceRepository.findServiceTotal(reservationId);

        // 총 결제 금액
        Long totalAmount = flightTotal + (serviceTotal != null ? serviceTotal : 0L);
        String orderName = "항공권 예약";
        String customerName = user.getUsername();
        String customerEmail = user.getUsername();

        // 주문 ID 생성
        String orderId = paymentService.generateOrderId(reservationId);

        // 토스 SDK에 전달할 데이터
        model.addAttribute("clientKey", tossConfig.getClientKey());
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", totalAmount);  // 실제 금액
        model.addAttribute("customerName", customerName);
        model.addAttribute("customerEmail", customerEmail);
        model.addAttribute("successUrl", tossConfig.getSuccessUrl());
        model.addAttribute("failUrl", tossConfig.getFailUrl());
        model.addAttribute("reservationId", reservationId);

        return "payment/payment";
    }

    /**
     * 결제 성공 콜백 (토스 → 우리 서버)
     *
     * 토스 결제창에서 결제 완료 후 리다이렉트됩니다.
     * 여기서 실제 결제 승인 API를 호출합니다.
     *
     * @param paymentKey 토스가 발급한 결제 키
     * @param orderId 우리가 생성한 주문 ID
     * @param amount 결제 금액
     */
    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            @AuthenticationPrincipal CustomUserDetails user,
            Model model) {

        log.info("결제콜백 성공 - paymentKey: {}, orderId: {}, amount: {}",
                paymentKey, orderId, amount);

        try {
            // 결제 승인 요청
            PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            PaymentViewDto result = paymentService.processPayment(request, user.getUserId());

            model.addAttribute("payment", result);
            model.addAttribute("success", true);

            return "payment/complete";  // 결제 완료 페이지

        } catch (Exception e) {
            log.error("결제콜백 승인 실패 - orderId: {}, error: {}", orderId, e.getMessage());

            model.addAttribute("success", false);
            model.addAttribute("errorMessage", e.getMessage());

            return "payment/complete";
        }
    }

    /**
     * 결제 실패 콜백
     *
     * 토스 결제창에서 결제 실패/취소 시 리다이렉트됩니다.
     */
    @GetMapping("/fail")
    public String paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String orderId,
            Model model) {

        log.warn("결제콜백 실패 - code: {}, message: {}, orderId: {}",
                code, message, orderId);

        model.addAttribute("success", false);
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);

        return "payment/complete";
    }
}