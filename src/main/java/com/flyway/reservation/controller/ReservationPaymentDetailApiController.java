package com.flyway.reservation.controller;

import com.flyway.reservation.dto.ReservationPaymentResponseDto;
import com.flyway.reservation.service.ReservationPaymentQueryService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationPaymentDetailApiController {

    private final ReservationPaymentQueryService reservationPaymentQueryService;

    /**
     * 예약 결제 상세 조회
     */
    @GetMapping("/{reservationId}/payment-detail")
    public ApiResponse<ReservationPaymentResponseDto> getReservationPaymentDetail(
            @PathVariable String reservationId
    ) {
        try {
            ReservationPaymentResponseDto detail = reservationPaymentQueryService.getReservationPaymentDetail(reservationId);
            return ApiResponse.success(detail);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get reservation payment detail", e);
            return ApiResponse.error(ErrorCode.RESERVATION_INTERNAL_ERROR.getCode(), ErrorCode.RESERVATION_INTERNAL_ERROR.getMessage());
        }
    }
}
