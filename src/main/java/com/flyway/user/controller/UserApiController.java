package com.flyway.user.controller;

import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.dto.UserProfileUpdateRequest;
import com.flyway.user.service.UserProfileService;
import com.flyway.user.service.UserWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserProfileService userProfileService;
    private final UserWithdrawalService userWithdrawalService;

    @GetMapping("/api/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> profile(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        String userId = (principal != null) ? principal.getUserId() : null;

        log.debug("[API] /api/profile userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(
                    ErrorCode.UNAUTHORIZED.getCode(),
                    ErrorCode.UNAUTHORIZED.getMessage()
            ));
        }

        UserProfileResponse profile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/api/user/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest request, HttpServletResponse response) {

        userWithdrawalService.withdraw(principal.getUserId(), LocalDateTime.now(), request, response);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody UserProfileUpdateRequest request
    ) {
        String userId = (principal != null) ? principal.getUserId() : null;

        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(
                    ErrorCode.UNAUTHORIZED.getCode(),
                    ErrorCode.UNAUTHORIZED.getMessage()
            ));
        }

        try {
            UserProfileResponse profile = userProfileService.updateProfile(userId, request);
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (Exception e) {
            log.error("[API] /api/profile update failed. userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                    ));
        }
    }

}
