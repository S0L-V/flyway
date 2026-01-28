package com.flyway.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.payment.config.TossPaymentsConfig;
import com.flyway.payment.domain.PaymentConfirmRequest;
import com.flyway.payment.domain.RefundRequest;
import com.flyway.payment.domain.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스 페이먼츠 API 호출 클라이언트
 *
 * 외부 API 호출을 담당하는 클래스입니다.
 * Service에서 직접 RestTemplate을 쓰지 않고 이 클래스를 통해 호출합니다.
 *
 * [토스 인증 방식]
 * - HTTP Basic Auth 사용
 * - 시크릿키를 Base64 인코딩해서 Authorization 헤더에 전달
 * - 형식: "Basic {base64(secretKey:)}"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final TossPaymentsConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 결제 승인 API 호출
     *
     * @param request paymentKey, orderId, amount
     * @return 토스 응답
     */
    public TossPaymentResponse confirmPayment(PaymentConfirmRequest request) {
        String url = config.getApiUrl() + "/payments/confirm";

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("[토스] 결제 승인 요청 - orderId: {}, amount: {}",
                request.getOrderId(), request.getAmount());

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );

            log.info("[토스] 결제 승인 성공 - paymentKey: {}", response.getBody().getPaymentKey());
            return response.getBody();

        } catch (Exception e) {
            log.error("[토스] 결제 승인 실패 - orderId: {}, error: {}",
                    request.getOrderId(), e.getMessage());
            throw new RuntimeException("토스 결제 승인 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 결제 취소(환불) API 호출
     *
     * @param request paymentKey, cancelReason, cancelAmount(선택)
     * @return 토스 응답
     */
    public TossPaymentResponse cancelPayment(RefundRequest request) {
        String url = config.getApiUrl() + "/payments/" + request.getPaymentKey() + "/cancel";

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        // 부분 취소일 경우에만 금액 포함
        if (request.getCancelAmount() != null) {
            body.put("cancelAmount", request.getCancelAmount());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("[토스] 결제 취소 요청 - paymentKey: {}, reason: {}",
                request.getPaymentKey(), request.getCancelReason());

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );

            log.info("[토스] 결제 취소 성공 - paymentKey: {}", request.getPaymentKey());
            return response.getBody();

        } catch (Exception e) {
            log.error("[토스] 결제 취소 실패 - paymentKey: {}, error: {}",
                    request.getPaymentKey(), e.getMessage());
            throw new RuntimeException("토스 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 토스 API 인증 헤더 생성
     *
     * 토스는 시크릿키를 Base64 인코딩해서 Basic Auth로 전달합니다.
     * 형식: "Basic {base64(secretKey:)}"
     * 주의: 시크릿키 뒤에 콜론(:)이 붙어야 함
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 시크릿키:를 Base64 인코딩
        String credentials = config.getSecretKey() + ":";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + encodedCredentials);

        return headers;
    }
}