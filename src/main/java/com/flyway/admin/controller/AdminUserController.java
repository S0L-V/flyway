package com.flyway.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flyway.admin.dto.AdminUserDto;
import com.flyway.admin.service.AdminUserService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService;

	@GetMapping
	public String userListPage() {
		return "admin/users";
	}

	/**
	 * 회원 통계 API
	 * GET /admin/users/api/stats
	 */
	@GetMapping("/api/stats")
	@ResponseBody
	public ApiResponse<Map<String, Integer>> getUserStats() {
		try {
			Map<String, Integer> stats = adminUserService.getUserStats();
			return ApiResponse.success(stats);
		} catch (Exception e) {
			log.error("Failed to get user stats", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}
	}

	/**
	 * 회원 목록 API (페이징, 검색, 필터)
	 * GET /admin/user/api/list
	 */
	@GetMapping("/api/list")
	@ResponseBody
	public ApiResponse<Map<String, Object>> getUserList(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String searchKeyword
	) {
		try {
			if(page < 1 || size < 1){
				return ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(),
					ErrorCode.INVALID_INPUT_VALUE.getMessage());
			}

			List<AdminUserDto> list = adminUserService.getUserList(status, searchKeyword, page, size);
			int totalCount = adminUserService.getUserCount(status, searchKeyword);

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("list", list);
			responseData.put("totalCount", totalCount);
			responseData.put("currentPage", page);
			responseData.put("pageSize", size);
			responseData.put("totalPages", (int)Math.ceil((double)totalCount / size));

			return ApiResponse.success(responseData);
		} catch (Exception e) {
			log.error("Failed to get user list", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}
	}

	/**
	 * 회원 상세 조회 API
	 * GET /admin/users/api/{id}
	 */
	@GetMapping("/api/{id}")
	@ResponseBody
	public ApiResponse<AdminUserDto> getUserById(@PathVariable String id) {
		try {
			AdminUserDto user = adminUserService.getUserById(id);
			if (user == null) {
				return ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "회원을 찾을 수 없습니다.");
			}
			return ApiResponse.success(user);
		} catch (Exception e) {
			log.error("Failed to get user by id: {}", id, e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}
	}

	/**
	 * 회원 상태 변경 API
	 * PUT /admin/users/api/{id}/status
	 */
	@PutMapping("/api/{id}/status")
	@ResponseBody
	public ApiResponse<Void> updateUserStatus(
		@PathVariable String id,
		@RequestBody Map<String, String> body
	) {
		try {
			String status = body.get("status");
			if (status == null || status.isEmpty()) {
				return ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), "상태값이 필요합니다.");
			}

			// ACTIVE 또는 BLOCKED만 허용
			if (!"ACTIVE".equals(status) && !"BLOCKED".equals(status)) {
				return ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(),
					ErrorCode.INVALID_INPUT_VALUE.getMessage());
			}

			boolean success = adminUserService.updateUserStatus(id, status);
			if (!success) {
				return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
					"상태 변경에 실패했습니다.");
			}

			return ApiResponse.success(null, "상태가 변경되었습니다.");
		} catch (Exception e) {
			log.error("Failed to update user status - id: {}", id, e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}
	}
}
