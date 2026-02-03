package com.flyway.payment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.payment.dto.RecentPaymentTickerDto;
import com.flyway.payment.mapper.PaymentMapper;
import com.flyway.payment.service.PaymentService;
import com.flyway.template.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메인 페이지 실시간 결제 티커 API (공개)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PaymentTickerApiController {

    private final PaymentService paymentService;

    @GetMapping("/payments/recent")
    public ApiResponse<List<RecentPaymentTickerDto>> getRecentPayments() {
        try {
            List<RecentPaymentTickerDto> recentPayments = paymentService.getRecentPaymentsForTicker();
            return ApiResponse.success(recentPayments);
        } catch (Exception e) {
            log.error("Failed to fetch recent payments for ticker", e);
            return ApiResponse.success(List.of()); // 에러 시 빈 리스트 반환
        }
    }
}
