package com.flyway.admin.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.repository.AdminDashboardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

	private final AdminDashboardRepository dashboardRepository;

	@Override
	@Transactional(readOnly = true)
	public DashboardStatsDto getStats(String adminId) {
		try {
			return DashboardStatsDto.builder()
				// 오늘 통계
				.dailyVisitors(safeCount(() -> dashboardRepository.countDailyVisitors()))
				.dailyReservations(safeCount(() -> dashboardRepository.countDailyReservations()))
				.dailyPayments(safeCount(() -> dashboardRepository.countDailyPayments()))
				.dailyCancellations(safeCount(() -> dashboardRepository.countDailyCancellations()))
				.dailyRevenue(safeCount(() -> dashboardRepository.sumDailyRevenue()))
				// 전체 통계
				.totalUsers(safeCount(() -> dashboardRepository.countTotalUsers()))
				.activeFlights(safeCount(() -> dashboardRepository.countActiveFlights()))
				// 실시간 상태
				.pendingReservations(safeCount(() -> dashboardRepository.countPendingReservations()))
				.pendingPayments(safeCount(() -> dashboardRepository.countPendingPayments()))
				// 알림
				.unreadNotifications(safeCount(() -> dashboardRepository.countUnreadNotifications(adminId)))
				.build();
		} catch (Exception e) {
			log.error("Failed to get dashboard stats", e);
			return DashboardStatsDto.builder().build();
		}
	}

	/**
	 * 최근 활동 목록 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RecentActivityDto> getRecentActivity(int limit) {
		try {
			return dashboardRepository.findRecentActivities(limit);
		} catch (Exception e) {
			log.error("Failed to get recent activities", e);
			return Collections.emptyList();
		}
	}

	/**
	 * 알림 목록 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AdminNotificationDto> getNotifications(String adminId, int limit) {
		try {
			return dashboardRepository.findNotifications(adminId, limit);
		} catch (Exception e) {
			log.error("Failed ot get notifications for adminId: {}", adminId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 알림 읽음 처리
	 */
	@Override
	@Transactional
	public boolean markNotificationAsRead(String notificationId, String adminId) {
		try {
			int updated = dashboardRepository.markNotificationAsRead(notificationId, adminId);
			return updated > 0;
		} catch (Exception e) {
			log.error("Failed to mark notifications as read: {}", notificationId, e);
			return false;
		}
	}

	/**
	 * 모든 알림 읽음 처리
	 */
	@Override
	@Transactional
	public int markAllNotificationsAsRead(String adminId) {
		try {
			return dashboardRepository.markAllNotificationsAsRead(adminId);
		} catch (Exception e) {
			log.error("Failed to mark all notifications as read for adminId: {}", adminId, e);
			return 0;
		}
	}

	/**
	 * 안전한 카운트 조회 (예외 발생 시 0 반환)
	 */
	private long safeCount(CountSupplier supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			log.warn("Count query failed, returning 0", e);
			return 0;
		}
	}

	@FunctionalInterface
	private interface CountSupplier {
		long get() throws Exception;
	}
}
