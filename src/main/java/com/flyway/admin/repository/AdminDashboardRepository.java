package com.flyway.admin.repository;

import java.util.List;
import java.util.Map;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.dto.VisitorDetailDto;

/**
 * 관리자 대시보드 Repository 인터페이스
 * Mapper를 래핑하여 비즈니스 로직 추상화
 */
public interface AdminDashboardRepository {

	// == 기간별 통계 조회 (첫 번째 줄) ==
	long countDailyVisitors();

	long countDailyPayments();

	long countDailyCancellations();

	long sumDailyRevenue();

	// == 실시간/전체 통계 (두 번째 줄) ==
	long countPendingReservations();

	long countTotalUsers();

	long countDailyNewUsers();

	long countActiveFlights();

	// == 방문자 상세 조회 ==
	List<VisitorDetailDto> findTodayVisitors(int limit);

	// == 최근 활동 조회 ==
	List<RecentActivityDto> findRecentActivities(int limit);

	// == 알림 조회 ==
	long countUnreadNotifications(String adminId);

	List<AdminNotificationDto> findNotifications(String adminId, int limit);

	int markNotificationAsRead(String notificationId, String adminId);

	int markAllNotificationsAsRead(String adminId);

	// == 차트용 통계 ==
	List<Map<String, Object>> findHourlyReservationDistribution(int days);
}
