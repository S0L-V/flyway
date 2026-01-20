package com.flyway.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.service.AdminDashboardService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardApiController {

	private final AdminDashboardService dashboardService;

	/**
	 * 대시보드 통계 조회
	 * GET /admin/api/dashboard/stats
	 */
	@GetMapping("/stats")
	public ApiResponse<DashboardStatsDto> getStats(HttpSession session) {
		try {
			String adminId = getAdminId(session);
			DashboardStatsDto stats = dashboardService.getStats(adminId);
			return ApiResponse.success(stats);
		} catch (Exception e) {
			log.error("Failed to get dashboard stats", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "통계 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 최근 활동 조회
	 * GET /admin/api/dashboard/activities?limit=10
	 */
	@GetMapping("/activities")
	public ApiResponse<List<RecentActivityDto>> getActivities(@RequestParam(defaultValue = "10") int limit) {
		try {
			List<RecentActivityDto> activities = dashboardService.getRecentActivity(limit);
			return ApiResponse.success(activities);
		} catch (Exception e) {
			log.error("Failed to get recent activities", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "활동 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 알림 목록 조회
	 * GET /admin/api/dashboard/notifications?limit=10
	 */
	@GetMapping("/notifications")
	public ApiResponse<List<AdminNotificationDto>> getNotifications(
		HttpSession session, @RequestParam(defaultValue = "10") int limit) {

		try {
			String adminId = getAdminId(session);
			List<AdminNotificationDto> notifications = dashboardService.getNotifications(adminId, limit);
			return ApiResponse.success(notifications);
		} catch (Exception e) {
			log.error("Failed to get notifications", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "알림 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 알림 읽음 처리
	 * POST /admin/api/dashboard/notifications/{id}/read
	 */
	@PostMapping("/notifications/{id}/read")
	public ApiResponse<Map<String, Object>> markNotificationAsRead(
		HttpSession session, @PathVariable("id") String notificationId) {

		try {
			String adminId = getAdminId(session);
			boolean success = dashboardService.markNotificationAsRead(notificationId, adminId);

			HashMap<String, Object> data = new HashMap<>();
			data.put("notificationId", notificationId);
			data.put("marked", success);

			if (success) {
				return ApiResponse.success(data, "알람을 읽음 처리했습니다.");
			} else {
				return ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "알림을 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			log.error("Failed to mark notification as read: {}", notificationId, e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "알림 처리 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 모든 알림 읽음 처리
	 * POST /admin/api/dashboard/notifications/read-all
	 */
	@PostMapping("/notifications/read-all")
	public ApiResponse<Map<String, Object>> markAllNotificationsAsRead(HttpSession session) {
		try {
			String adminId = getAdminId(session);
			int count = dashboardService.markAllNotificationsAsRead(adminId);

			Map<String, Object> data = new HashMap<>();
			data.put("markCount", count);

			return ApiResponse.success(data, count + "개의 알림을 읽음 처리했습니다.");
		} catch (Exception e) {
			log.error("Failed to mark all notifications as read", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "알림 처리 중 오류가 발생했습니다.");
		}
	}

	@GetMapping("/all")
	public ApiResponse<Map<String, Object>> getAllDashboardData(
		HttpSession session,
		@RequestParam(defaultValue = "10") int activitiesLimit,
		@RequestParam(defaultValue = "10") int notificationsLimit) {

		try {
			String adminId = getAdminId(session);

			Map<String, Object> data = new HashMap<>();
			data.put("stats", dashboardService.getStats(adminId));
			data.put("activities", dashboardService.getRecentActivity(activitiesLimit));
			data.put("notifications", dashboardService.getNotifications(adminId, notificationsLimit));

			return ApiResponse.success(data);
		} catch (Exception e) {
			log.error("Failed to get all dashboard data", e);
			return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "대시보드 데이터 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 세션에서 관리자 ID 추출
	 */
	private String getAdminId(HttpSession session) {
		Object adminId = session.getAttribute("adminId");
		return adminId != null ? adminId.toString() : null;
	}
}
