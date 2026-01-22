package com.flyway.admin.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.repository.AdminDashboardRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 서비스 테스트")
class AdminDashboardServiceImplTest {
	@Mock
	private AdminDashboardRepository dashboardRepository;

	@InjectMocks
	private AdminDashboardServiceImpl dashboardService;

	private static final String TEST_ADMIN_ID = "test-admin-id";

	@Nested
	@DisplayName("대시보드 통계 조회")
	class GetStatsTest {

		@Test
		@DisplayName("모든 통계 조회 성공")
		void getStats_Success() {
			// given
			given(dashboardRepository.countDailyVisitors()).willReturn(100L);
			given(dashboardRepository.countDailyReservations()).willReturn(50L);
			given(dashboardRepository.countDailyPayments()).willReturn(30L);
			given(dashboardRepository.countDailyCancellations()).willReturn(5L);
			given(dashboardRepository.sumDailyRevenue()).willReturn(5000000L);
			given(dashboardRepository.countTotalUsers()).willReturn(1000L);
			given(dashboardRepository.countActiveFlights()).willReturn(200L);
			given(dashboardRepository.countPendingReservations()).willReturn(10L);
			given(dashboardRepository.countPendingPayments()).willReturn(3L);
			given(dashboardRepository.countUnreadNotifications(TEST_ADMIN_ID)).willReturn(7L);

			// when
			DashboardStatsDto stats = dashboardService.getStats(TEST_ADMIN_ID);

			// then
			assertThat(stats).isNotNull();
			assertThat(stats.getDailyVisitors()).isEqualTo(100L);
			assertThat(stats.getDailyReservations()).isEqualTo(50L);
			assertThat(stats.getDailyPayments()).isEqualTo(30L);
			assertThat(stats.getDailyCancellations()).isEqualTo(5L);
			assertThat(stats.getDailyRevenue()).isEqualTo(5000000L);
			assertThat(stats.getTotalUsers()).isEqualTo(1000L);
			assertThat(stats.getActiveFlights()).isEqualTo(200L);
			assertThat(stats.getPendingReservations()).isEqualTo(10L);
			assertThat(stats.getPendingPayments()).isEqualTo(3L);
			assertThat(stats.getUnreadNotifications()).isEqualTo(7L);
		}

		@Test
		@DisplayName("일부 쿼리 실패 시 해당 값 0 반환")
		void getStats_PartialFailure_ReturnsZero() {
			// given
			given(dashboardRepository.countDailyVisitors()).willReturn(100L);
			given(dashboardRepository.countDailyReservations()).willThrow(new RuntimeException("DB Error"));
			given(dashboardRepository.countDailyPayments()).willReturn(30L);
			given(dashboardRepository.countDailyCancellations()).willReturn(5L);
			given(dashboardRepository.sumDailyRevenue()).willReturn(5000000L);
			given(dashboardRepository.countTotalUsers()).willReturn(1000L);
			given(dashboardRepository.countActiveFlights()).willReturn(200L);
			given(dashboardRepository.countPendingReservations()).willReturn(10L);
			given(dashboardRepository.countPendingPayments()).willReturn(3L);
			given(dashboardRepository.countUnreadNotifications(TEST_ADMIN_ID)).willReturn(7L);

			// when
			DashboardStatsDto stats = dashboardService.getStats(TEST_ADMIN_ID);

			// then
			assertThat(stats).isNotNull();
			assertThat(stats.getDailyVisitors()).isEqualTo(100L);
			assertThat(stats.getDailyReservations()).isEqualTo(0L); // 실패 시 0
			assertThat(stats.getDailyPayments()).isEqualTo(30L);
		}

		@Test
		@DisplayName("adminId가 null인 경우")
		void getStats_NullAdminId() {
			// given
			given(dashboardRepository.countDailyVisitors()).willReturn(100L);
			given(dashboardRepository.countDailyReservations()).willReturn(50L);
			given(dashboardRepository.countDailyPayments()).willReturn(30L);
			given(dashboardRepository.countDailyCancellations()).willReturn(5L);
			given(dashboardRepository.sumDailyRevenue()).willReturn(5000000L);
			given(dashboardRepository.countTotalUsers()).willReturn(1000L);
			given(dashboardRepository.countActiveFlights()).willReturn(200L);
			given(dashboardRepository.countPendingReservations()).willReturn(10L);
			given(dashboardRepository.countPendingPayments()).willReturn(3L);
			given(dashboardRepository.countUnreadNotifications(null)).willReturn(0L);

			// when
			DashboardStatsDto stats = dashboardService.getStats(null);

			// then
			assertThat(stats).isNotNull();
		}
	}

	@Nested
	@DisplayName("최근 활동 조회")
	class GetRecentActivitiesTest {

		@Test
		@DisplayName("최근 활동 조회 성공")
		void getRecentActivities_Success() {
			// given
			List<RecentActivityDto> mockActivities = Arrays.asList(
				RecentActivityDto.builder()
					.activityId("RES_001")
					.activityType("RESERVATION")
					.description("ICN → NRT 예약")
					.userName("홍 길동")
					.userEmail("hong@test.com")
					.status("HELD")
					.createdAt(LocalDateTime.now())
					.build(),
				RecentActivityDto.builder()
					.activityId("PAY_001")
					.activityType("PAYMENT")
					.description("결제 완료 ₩500,000")
					.userName("김 철수")
					.userEmail("kim@test.com")
					.amount(500000L)
					.status("PAID")
					.createdAt(LocalDateTime.now().minusHours(1))
					.build()
			);
			given(dashboardRepository.findRecentActivities(10)).willReturn(mockActivities);

			// when
			List<RecentActivityDto> result = dashboardService.getRecentActivity(10);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getActivityType()).isEqualTo("RESERVATION");
			assertThat(result.get(1).getActivityType()).isEqualTo("PAYMENT");
			assertThat(result.get(1).getAmount()).isEqualTo(500000L);
		}

		@Test
		@DisplayName("활동 없을 때 빈 리스트 반환")
		void getRecentActivities_Empty() {
			// given
			given(dashboardRepository.findRecentActivities(10)).willReturn(Collections.emptyList());

			// when
			List<RecentActivityDto> result = dashboardService.getRecentActivity(10);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("조회 실패 시 빈 리스트 반환")
		void getRecentActivities_Error_ReturnsEmptyList() {
			// given
			given(dashboardRepository.findRecentActivities(anyInt()))
				.willThrow(new RuntimeException("DB Error"));

			// when
			List<RecentActivityDto> result = dashboardService.getRecentActivity(10);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("알림 조회")
	class GetNotificationsTest {

		@Test
		@DisplayName("알림 조회 성공")
		void getNotifications_Success() {
			// given
			List<AdminNotificationDto> mockNotifications = Arrays.asList(
				AdminNotificationDto.builder()
					.notificationId("notif-001")
					.notificationType("NEW_RESERVATION")
					.title("새 예약")
					.message("새로운 예약이 접수되었습니다.")
					.isRead("N")
					.priority("NORMAL")
					.createdAt(LocalDateTime.now())
					.build(),
				AdminNotificationDto.builder()
					.notificationId("notif-002")
					.notificationType("REFUND_REQUEST")
					.title("환불 요청")
					.message("환불 요청이 접수되었습니다.")
					.isRead("N")
					.priority("HIGH")
					.createdAt(LocalDateTime.now().minusHours(2))
					.build()
			);
			given(dashboardRepository.findNotifications(TEST_ADMIN_ID, 10)).willReturn(mockNotifications);

			// when
			List<AdminNotificationDto> result = dashboardService.getNotifications(TEST_ADMIN_ID, 10);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getNotificationType()).isEqualTo("NEW_RESERVATION");
			assertThat(result.get(1).getPriority()).isEqualTo("HIGH");
		}

		@Test
		@DisplayName("조회 실패 시 빈 리스트 반환")
		void getNotifications_Error_ReturnsEmptyList() {
			// given
			given(dashboardRepository.findNotifications(anyString(), anyInt()))
				.willThrow(new RuntimeException("DB Error"));

			// when
			List<AdminNotificationDto> result = dashboardService.getNotifications(TEST_ADMIN_ID, 10);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("알림 읽음 처리")
	class MarkNotificationAsReadTest {

		@Test
		@DisplayName("알림 읽음 처리 성공")
		void markNotificationAsRead_Success() {
			// given
			String notificationId = "notif-001";
			given(dashboardRepository.markNotificationAsRead(notificationId, TEST_ADMIN_ID)).willReturn(1);

			// when
			boolean result = dashboardService.markNotificationAsRead(notificationId, TEST_ADMIN_ID);

			// then
			assertThat(result).isTrue();
			then(dashboardRepository).should().markNotificationAsRead(notificationId, TEST_ADMIN_ID);
		}

		@Test
		@DisplayName("존재하지 않는 알림 - false 반환")
		void markNotificationAsRead_NotFound() {
			// given
			String notificationId = "notif-not-exist";
			given(dashboardRepository.markNotificationAsRead(notificationId, TEST_ADMIN_ID)).willReturn(0);

			// when
			boolean result = dashboardService.markNotificationAsRead(notificationId, TEST_ADMIN_ID);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("읽음 처리 실패 - false 반환")
		void markNotificationAsRead_Error_ReturnsFalse() {
			// given
			given(dashboardRepository.markNotificationAsRead(anyString(), anyString()))
				.willThrow(new RuntimeException("DB Error"));

			// when
			boolean result = dashboardService.markNotificationAsRead("notif-001", TEST_ADMIN_ID);

			// then
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("모든 알림 읽음 처리")
	class MarkAllNotificationsAsReadTest {

		@Test
		@DisplayName("모든 알림 읽음 처리 성공")
		void markAllNotificationsAsRead_Success() {
			// given
			given(dashboardRepository.markAllNotificationsAsRead(TEST_ADMIN_ID)).willReturn(5);

			// when
			int result = dashboardService.markAllNotificationsAsRead(TEST_ADMIN_ID);

			// then
			assertThat(result).isEqualTo(5);
			then(dashboardRepository).should().markAllNotificationsAsRead(TEST_ADMIN_ID);
		}

		@Test
		@DisplayName("읽을 알림 없음 - 0 반환")
		void markAllNotificationsAsRead_NoNotifications() {
			// given
			given(dashboardRepository.markAllNotificationsAsRead(TEST_ADMIN_ID)).willReturn(0);

			// when
			int result = dashboardService.markAllNotificationsAsRead(TEST_ADMIN_ID);

			// then
			assertThat(result).isEqualTo(0);
		}

		@Test
		@DisplayName("처리 실패 - 0 반환")
		void markAllNotificationsAsRead_Error_ReturnsZero() {
			// given
			given(dashboardRepository.markAllNotificationsAsRead(anyString()))
				.willThrow(new RuntimeException("DB Error"));

			// when
			int result = dashboardService.markAllNotificationsAsRead(TEST_ADMIN_ID);

			// then
			assertThat(result).isEqualTo(0);
		}
	}
}