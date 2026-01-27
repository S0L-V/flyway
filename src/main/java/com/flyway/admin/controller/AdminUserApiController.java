package com.flyway.admin.controller;

import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserApiController {

    private final UserQueryService userQueryService;

    /**
     * 회원 목록 조회
     *  GET /admin/api/users
     *  GET /admin/api/users?status=ACTIVE
     */
    @GetMapping
    public ApiResponse<List<UserFullJoinRow>> getUsers(
            @RequestParam(required = false) AuthStatus status
    ) {
        try {
            List<UserFullJoinRow> users = userQueryService.getUsers(status);
            return ApiResponse.success(users);
        } catch (Exception e) {
            log.error("Failed to get user list", e);
            return ApiResponse.error(ErrorCode.USER_INTERNAL_ERROR.getCode(), ErrorCode.USER_INTERNAL_ERROR.getMessage());
        }
    }

    /**
     * 회원 단건 조회
     *  GET /admin/api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserFullJoinRow> getUserDetail(
            @PathVariable String userId
    ) {
        try {
            UserFullJoinRow user = userQueryService.getUserDetail(userId);
            return ApiResponse.success(user);
        } catch (Exception e) {
            log.error("Failed to get user detail", e);
            return ApiResponse.error(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
        }
    }
}
