package com.flyway.user.controller;

import com.flyway.reservation.dto.ReservationDetailDto;
import com.flyway.reservation.dto.ReservationDetailResponseDto;
import com.flyway.reservation.dto.ReservationSummaryDto;
import com.flyway.reservation.service.ReservationQueryService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.common.PageResult;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserReservationApiController {

    private final ReservationQueryService reservationQueryService;

    @GetMapping("/reservations")
    public ApiResponse<List<ReservationSummaryDto>> myReservations(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            PageResult<ReservationSummaryDto> result = reservationQueryService.getUserReservationHistories(
                    principal.getUserId(), page, size
            );
            return ApiResponse.success(result.getData(), result.getPage());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get reservation list", e);
            return ApiResponse.error(ErrorCode.RESERVATION_INTERNAL_ERROR.getCode(), ErrorCode.RESERVATION_INTERNAL_ERROR.getMessage());
        }
    }

    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<ReservationDetailResponseDto> reservationDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String reservationId
    ) {
        try {
            ReservationDetailDto detail = reservationQueryService.getUserReservationDetail(
                    principal.getUserId(), reservationId
            );
            return ApiResponse.success(ReservationDetailResponseDto.from(detail));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get reservation detail", e);
            return ApiResponse.error(ErrorCode.RESERVATION_INTERNAL_ERROR.getCode(), ErrorCode.RESERVATION_INTERNAL_ERROR.getMessage());
        }
    }
}
