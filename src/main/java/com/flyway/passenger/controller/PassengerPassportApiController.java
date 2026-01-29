package com.flyway.passenger.controller;

import com.flyway.passenger.dto.PassengerPassportUpdateRequestDto;
import com.flyway.passenger.service.PassengerPassportService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/reservations")
public class PassengerPassportApiController {

    private final PassengerPassportService passengerPassportService;

    @PatchMapping("/{reservationId}/passengers/{passengerId}/passport")
    public ApiResponse<Void> updatePassport(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String reservationId,
            @PathVariable String passengerId,
            @RequestBody PassengerPassportUpdateRequestDto request
    ) {
        try {
            String userId = principal != null ? principal.getUserId() : null;
            if (userId == null || userId.isBlank()) {
                return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
            }
            passengerPassportService.updatePassport(userId, reservationId, passengerId, request);
            return ApiResponse.success(null, "여권 정보가 저장되었습니다.");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update passenger passport", e);
            return ApiResponse.error(ErrorCode.RESERVATION_INTERNAL_ERROR.getCode(), ErrorCode.RESERVATION_INTERNAL_ERROR.getMessage());
        }
    }
}
