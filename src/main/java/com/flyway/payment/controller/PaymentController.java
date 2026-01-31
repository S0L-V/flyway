package com.flyway.payment.controller;

import com.flyway.payment.config.TossPaymentsConfig;
import com.flyway.payment.domain.PaymentConfirmRequest;
import com.flyway.payment.dto.PaymentViewDto;
import com.flyway.payment.service.PaymentService;
import com.flyway.reservation.dto.ReservationSegmentView;
import com.flyway.passenger.repository.PassengerServiceRepository;
import com.flyway.reservation.repository.ReservationBookingRepository;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.user.domain.UserProfile;
import com.flyway.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.flyway.template.util.MaskUtil.maskEmail;

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
    private final UserProfileMapper userProfileMapper;
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
        UserProfile profile = userProfileMapper.findByUserId(user.getUserId());

        // 이름: 프로필에서 가져오기 (없으면 이메일 @ 앞부분)
        String customerName = (profile != null && profile.getName() != null)
                ? profile.getName()
                : user.getUser().getEmail().split("@")[0];

        // 이메일: 마스킹
        String customerEmail = maskEmail(user.getUser().getEmail());
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
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];  // @ 앞부분
        String domain = parts[1];     // @ 뒷부분

        // 앞 3자리만 표시
        String maskedLocal = localPart.length() > 3
                ? localPart.substring(0, 3) + "***"
                : localPart + "***";

        // 도메인도 마스킹
        String maskedDomain = domain.length() > 4
                ? "***" + domain.substring(domain.lastIndexOf("."))
                : "***";

        return maskedLocal + "@" + maskedDomain;
    }
    /**
     * 결제 성공 콜백 (토스 → 우리 서버)
     *
     * 토스 결제창에서 결제 완료 후 리다이렉트
     * 결제 승인 API 호출.
     * @param paymentKey 토스가 발급한 결제 키
     * @param orderId 플라이웨이 주문 ID
     * @param amount 결제 금액
     */
    //==================================
    /**
     * 변경사항
     * @AuthenticationPrincipal 제거로 쿠키 의존 x smasite lax 떄문
     * orderId 서버 검증으로 변경=
     */

    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {

        log.info("결제콜백 - orderId: {}", orderId);

        try {
            // 결제 승인 요청
            PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            PaymentViewDto result = paymentService.processPaymentByOrderId(request);

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
    @GetMapping("/refund-test")
    public String refundTestPage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        log.info("[환불 테스트 페이지] 진입 - userId: {}", user.getUserId());

        // 사용자 결제 내역 조회
        List<PaymentViewDto> userPayments = paymentService.findPaymentsByUserId(user.getUserId());

        // Debugging: Log payment IDs before sending to JSP
        if (userPayments != null && !userPayments.isEmpty()) {
            userPayments.forEach(payment -> {
                log.info("Payment fetched for user {}: paymentId = {}", user.getUserId(), payment.getPaymentId());
            });
        } else {
            log.info("No payments found for user {}", user.getUserId());
        }

        model.addAttribute("userPayments", userPayments);

        return "payment/refund-test"; // payment/refund-test.jsp
    }
}