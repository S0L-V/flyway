package com.flyway.admin.controller;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.ApiResponse;
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
     *  GET /api/admin/users
     *  GET /api/admin/users?status=ACTIVE
     */
    @GetMapping
    public ApiResponse<List<UserFullJoinRow>> getUsers(
            @RequestParam(required = false) AuthStatus status
    ) {
        List<UserFullJoinRow> users = userQueryService.getUsers(status);
        return ApiResponse.success(users);
    }

    /**
     * 회원 단건 조회
     *  GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserFullJoinRow> getUserDetail(
            @PathVariable String userId
    ) {
        UserFullJoinRow user = userQueryService.getUserDetail(userId);
        return ApiResponse.success(user);
    }
}
