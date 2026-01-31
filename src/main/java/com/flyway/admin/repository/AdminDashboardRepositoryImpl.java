package com.flyway.admin.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.dto.VisitorDetailDto;
import com.flyway.admin.mapper.AdminDashboardMapper;
import com.flyway.admin.mapper.VisitorLogMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminDashboardRepositoryImpl implements AdminDashboardRepository {

	private final AdminDashboardMapper dashboardMapper;
	private final VisitorLogMapper visitorLogMapper;

	// == 기간별 통계 (첫 번째 줄) ==
	@Override
	public long countDailyVisitors() {
		return dashboardMapper.countDailyVisitors();
	}

	@Override
	public long countDailyPayments() {
		return dashboardMapper.countDailyPayments();
	}

	@Override
	public long countDailyCancellations() {
		return dashboardMapper.countDailyCancellations();
	}

	@Override
	public long sumDailyRevenue() {
		return dashboardMapper.sumDailyRevenue();
	}

	// == 실시간/전체 통계 (두 번째 줄) ==
	@Override
	public long countPendingReservations() {
		return dashboardMapper.countPendingReservations();
	}

	@Override
	public long countTotalUsers() {
		return dashboardMapper.countTotalUsers();
	}

	@Override
	public long countDailyNewUsers() {
		return dashboardMapper.countDailyNewUsers();
	}

	@Override
	public long countActiveFlights() {
		return dashboardMapper.countActiveFlights();
	}

	@Override
	public List<RecentActivityDto> findRecentActivities(int limit) {
		log.debug("Finding recent activities, limit: {}", limit);
		return dashboardMapper.selectRecentActivities(limit);
	}

	@Override
	public long countUnreadNotifications(String adminId) {
		return dashboardMapper.countUnreadNotifications(adminId);
	}

	@Override
	public List<AdminNotificationDto> findNotifications(String adminId, int limit) {
		log.debug("Finding notifications for admin: {}, limit: {}", adminId, limit);
		return dashboardMapper.selectNotifications(adminId, limit);
	}

	@Override
	public int markNotificationAsRead(String notificationId, String adminId) {
		int updated = dashboardMapper.markNotificationAsRead(notificationId, adminId);
		if (updated > 0) {
			log.debug("Marked notification as read: {}", notificationId);
		}
		return updated;
	}

	@Override
	public int markAllNotificationsAsRead(String adminId) {
		int updated = dashboardMapper.markAllNotificationsAsRead(adminId);
		log.debug("Marked {} notifications as read for admin: {}", updated, adminId);
		return updated;
	}

	// == 방문자 상세 조회 ==
	@Override
	public List<VisitorDetailDto> findTodayVisitors(int limit) {
		log.debug("Finding today visitors, limit: {}", limit);
		return visitorLogMapper.selectTodayVisitors(limit);
	}

	// == 차트용 통계 ==
	@Override
	public List<Map<String, Object>> findHourlyReservationDistribution(int days) {
		log.debug("Finding hourly reservation distribution for {} days", days);
		return dashboardMapper.selectHourlyReservationDistribution(days);
	}
}
