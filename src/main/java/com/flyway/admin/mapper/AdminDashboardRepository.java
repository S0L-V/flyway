package com.flyway.admin.mapper;

import java.util.List;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.RecentActivityDto;

/**
 * 관리자 대시보드 Repository 인터페이스
 * Mapper를 래핑하여 비즈니스 로직 추상화
 */
public interface AdminDashboardRepository {

	// == 통계 조회 ==
	long countDailyVisitors();

	long countDailyReservations();

	long countDailyPayments();

	long countDailyCancellations();

	long sumDailyRevenue();

	long countTotalUsers();

	long countActiveFlights();

	long countPendingReservations();

	long countPendingPayments();

	// == 최근 활동 조회 ==
	List<RecentActivityDto> findRecentActivities(int limit);

	// == 알림 조회 ==
	long countUnreadNotifications(String adminId);

	List<AdminNotificationDto> findNotifications(String adminId, int limit);

	int markNotificationAsRead(String notificationId, String adminId);

	int markAllNotificationsAsRead(String adminId);
}
