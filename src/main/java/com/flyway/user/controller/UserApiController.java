package com.flyway.user.controller;

import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserProfileService userProfileService;

    @GetMapping("/api/profile")
    public ApiResponse<UserProfileResponse> profile(@AuthenticationPrincipal CustomUserDetails principal) {
        String userId = (principal != null) ? principal.getUserId() : null;

        log.debug("[API] /api/profile userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            return ApiResponse.error(
                    ErrorCode.UNAUTHORIZED.getCode(),
                    ErrorCode.UNAUTHORIZED.getMessage()
            );
        }

        UserProfileResponse profile = userProfileService.getUserProfile(userId);
        return ApiResponse.success(profile);
    }
}
