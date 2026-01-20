package com.flyway.admin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.mapper.AdminDashboardMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminDashboardRepositoryImpl implements AdminDashboardRepository {

	private final AdminDashboardMapper dashboardMapper;

	@Override
	public long countDailyVisitors() {
		return dashboardMapper.countDailyVisitors();
	}

	@Override
	public long countDailyReservations() {
		return dashboardMapper.countPendingReservations();
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

	@Override
	public long countTotalUsers() {
		return dashboardMapper.countTotalUsers();
	}

	@Override
	public long countActiveFlights() {
		return dashboardMapper.countActiveFlights();
	}

	@Override
	public long countPendingReservations() {
		return dashboardMapper.countPendingReservations();
	}

	@Override
	public long countPendingPayments() {
		return dashboardMapper.countPendingPayments();
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
}
