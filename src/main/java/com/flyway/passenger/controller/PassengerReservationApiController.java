package com.flyway.passenger.controller;

import com.flyway.passenger.dto.ReservationPassengersResponseDto;
import com.flyway.passenger.service.PassengerQueryService;
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
public class PassengerReservationApiController {

    private final PassengerQueryService passengerQueryService;

    @GetMapping("/{reservationId}/passengers")
    public ApiResponse<ReservationPassengersResponseDto> reservationPassengers(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String reservationId
    ) {
        try {
            ReservationPassengersResponseDto response = passengerQueryService.getReservationPassengers(reservationId);
            return ApiResponse.success(response);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get reservation passengers", e);
            return ApiResponse.error(ErrorCode.RESERVATION_INTERNAL_ERROR.getCode(), ErrorCode.RESERVATION_INTERNAL_ERROR.getMessage());
        }
    }
}
