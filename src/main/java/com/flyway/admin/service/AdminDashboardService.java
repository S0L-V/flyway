package com.flyway.admin.service;

import java.util.List;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;

public interface AdminDashboardService {

	/**
	 * 대시 보드 통계 조회
	 */
	DashboardStatsDto getStats(String adminId);

	/**
	 * 최근 활동 목록 조회
	 */
	List<RecentActivityDto> getRecentActivity(int limit);

	/**
	 * 알림 목록 조회
	 */
	List<AdminNotificationDto> getNotifications(String adminId, int limit);

	/**
	 * 알림 읽음 처리
	 */
	boolean markNotificationAsRead(String notificationId, String adminId);

	/**
	 * 모든 알림 읽음 처리
	 */
	int markAllNotificationsAsRead(String adminId);
}
