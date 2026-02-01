package com.flyway.admin.service;

import java.util.List;
import java.util.Map;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.dto.StatisticsDto;
import com.flyway.admin.dto.VisitorDetailDto;

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

	/**
	 * 기간별 통계 조회 (주간/월간)
	 */
	StatisticsDto getPeriodStats(String period);

	/**
	 * 최근 N일간 일일 통계 목록 조회 (차트용)
	 */
	List<StatisticsDto> getRecentDailyStats(int days);

	/**
	 * 오늘 방문자 상세 목록 조회
	 */
	List<VisitorDetailDto> getTodayVisitors(int limit);

	/**
	 * 시간대별 예약 분포 조회 (차트용)
	 * @param days 조회 기간 (일)
	 * @return 시간대(0-23)별 예약 건수 리스트
	 */
	List<Map<String, Object>> getHourlyReservationDistribution(int days);

	/**
	 * 예약 상태별 분포 조회 (차트용)
	 * @param days 조회 기간 (일)
	 * @return 상태별 예약 건수 리스트
	 */
	List<Map<String, Object>> getReservationStatusDistribution(int days);
}
