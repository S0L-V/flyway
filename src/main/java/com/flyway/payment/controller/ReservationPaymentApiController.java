package com.flyway.payment.controller;

import com.flyway.payment.dto.PaymentDto;
import com.flyway.payment.service.PaymentQueryService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/reservations")
public class ReservationPaymentApiController {

    private final PaymentQueryService paymentQueryService;

    @GetMapping("/{reservationId}/payments")
    public ApiResponse<PaymentDto> getReservationPayments(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String reservationId
    ) {
        try {
            if (principal == null || principal.getUserId() == null || principal.getUserId().isBlank()) {
                return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
            }
            PaymentDto payment = paymentQueryService.getLatestPaidByReservationIdAndUserId(reservationId, principal.getUserId());
            return ApiResponse.success(payment);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get reservation payments", e);
            return ApiResponse.error(ErrorCode.PAYMENT_LIST_FETCH_FAILED.getCode(), ErrorCode.PAYMENT_LIST_FETCH_FAILED.getMessage());
        }
    }
}
