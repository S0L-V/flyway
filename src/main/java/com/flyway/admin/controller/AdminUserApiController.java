package com.flyway.admin.controller;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.service.UserQueryService;
import com.flyway.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserApiController {

    private final UserQueryService userQueryService;
    private final UserService userService;

    /**
     * 회원 목록 조회
     * GET /admin/api/users
     * GET /admin/api/users?status=ACTIVE
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
     * GET /admin/api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserFullJoinRow> getUserDetail(
            @PathVariable String userId
    ) {
        try {
            UserFullJoinRow user = userQueryService.getUserDetail(userId);
            return ApiResponse.success(user);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to get user detail", e);
            return ApiResponse.error(ErrorCode.USER_INTERNAL_ERROR.getCode(), ErrorCode.USER_INTERNAL_ERROR.getMessage());
        }
    }

    /**
     * 회원 차단
     * POST /admin/api/users/{userId}/block
     */
    @PostMapping("/{userId}/block")
    public ApiResponse<Map<String, Object>> blockUser(
            @PathVariable String userId
    ) {
        try {
            AuthStatus status = userService.blockUser(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("status", status);
            return ApiResponse.success(data, "회원 차단 완료");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to block user: {}", userId, e);
            return ApiResponse.error(ErrorCode.USER_INTERNAL_ERROR.getCode(), ErrorCode.USER_INTERNAL_ERROR.getMessage());
        }
    }

    /**
     * 회원 차단 해제
     * POST /admin/api/users/{userId}/unblock
     */
    @PostMapping("/{userId}/unblock")
    public ApiResponse<Map<String, Object>> unblockUser(
            @PathVariable String userId
    ) {
        try {
            AuthStatus status = userService.unblockUser(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("status", status);
            return ApiResponse.success(data, "회원 차단 해제 완료");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error("Failed to unblock user: {}", userId, e);
            return ApiResponse.error(ErrorCode.USER_INTERNAL_ERROR.getCode(), ErrorCode.USER_INTERNAL_ERROR.getMessage());
        }
    }
}
