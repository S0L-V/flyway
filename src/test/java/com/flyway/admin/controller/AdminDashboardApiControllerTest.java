package com.flyway.admin.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.service.AdminDashboardService;
import com.flyway.template.common.ApiResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 대시보드 API 컨트롤러 테스트")
class AdminDashboardApiControllerTest {

	@Mock
	private AdminDashboardService dashboardService;

	@InjectMocks
	private AdminDashboardApiController controller;

	private MockHttpSession session;
	private static final String TEST_ADMIN_ID = "test-admin-id";

	@BeforeEach
	void setUp() {
		session = new MockHttpSession();
		session.setAttribute("adminId", TEST_ADMIN_ID);
	}

	@Nested
	@DisplayName("통계 조회 API")
	class GetStatsTest {

		@Test
		@DisplayName("통계 조회 성공")
		void getStats_Success() {
			// given
			DashboardStatsDto stats = DashboardStatsDto.builder()
				.dailyVisitors(100)
				.dailyPayments(30)
				.dailyCancellations(5)
				.dailyRevenue(5000000)
				.totalUsers(1000)
				.activeFlights(200)
				.pendingReservations(10)
				.unreadNotifications(7)
				.build();
			given(dashboardService.getStats(TEST_ADMIN_ID)).willReturn(stats);

			// when
			ApiResponse<DashboardStatsDto> response = controller.getStats(session);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData().getDailyVisitors()).isEqualTo(100);
		}

		@Test
		@DisplayName("통계 조회 실패 - 서비스 예외")
		void getStats_ServiceError() {
			// given
			given(dashboardService.getStats(anyString())).willThrow(new RuntimeException("Service Error"));

			// when
			ApiResponse<DashboardStatsDto> response = controller.getStats(session);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("최근 활동 조회 API")
	class GetActivitiesTest {

		@Test
		@DisplayName("최근 활동 조회 성공")
		void getActivities_Success() {
			// given
			List<RecentActivityDto> activities = Arrays.asList(
				RecentActivityDto.builder()
					.activityId("RES_001")
					.activityType("RESERVATION")
					.description("ICN → NRT 예약")
					.userName("홍 길동")
					.userEmail("hong@test.com")
					.status("HELD")
					.createdAt(LocalDateTime.now())
					.build()
			);
			given(dashboardService.getRecentActivity(10)).willReturn(activities);

			// when
			ApiResponse<List<RecentActivityDto>> response = controller.getActivities(10);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData()).hasSize(1);
			assertThat(response.getData().get(0).getActivityType()).isEqualTo("RESERVATION");
		}

		@Test
		@DisplayName("최근 활동 조회 실패")
		void getActivities_Error() {
			// given
			given(dashboardService.getRecentActivity(anyInt())).willThrow(new RuntimeException("Error"));

			// when
			ApiResponse<List<RecentActivityDto>> response = controller.getActivities(10);

			// then
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("알림 조회 API")
	class GetNotificationsTest {

		@Test
		@DisplayName("알림 조회 성공")
		void getNotifications_Success() {
			// given
			List<AdminNotificationDto> notifications = Arrays.asList(
				AdminNotificationDto.builder()
					.notificationId("notif-001")
					.notificationType("NEW_RESERVATION")
					.title("새 예약")
					.message("새로운 예약이 접수되었습니다.")
					.isRead("N")
					.priority("NORMAL")
					.createdAt(LocalDateTime.now())
					.build()
			);
			given(dashboardService.getNotifications(TEST_ADMIN_ID, 10)).willReturn(notifications);

			// when
			ApiResponse<List<AdminNotificationDto>> response = controller.getNotifications(session, 10);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData()).hasSize(1);
		}

		@Test
		@DisplayName("알림 조회 실패")
		void getNotifications_Error() {
			// given
			given(dashboardService.getNotifications(anyString(), anyInt())).willThrow(new RuntimeException("Error"));

			// when
			ApiResponse<List<AdminNotificationDto>> response = controller.getNotifications(session, 10);

			// then
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("알림 읽음 처리 API")
	class MarkNotificationAsReadTest {

		@Test
		@DisplayName("알림 읽음 처리 성공")
		void markNotificationAsRead_Success() {
			// given
			String notificationId = "notif-001";
			given(dashboardService.markNotificationAsRead(notificationId, TEST_ADMIN_ID)).willReturn(true);

			// when
			ApiResponse<Map<String, Object>> response = controller.markNotificationAsRead(session, notificationId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData().get("notificationId")).isEqualTo(notificationId);
			assertThat(response.getData().get("marked")).isEqualTo(true);
		}

		@Test
		@DisplayName("존재하지 않는 알림")
		void markNotificationAsRead_NotFound() {
			// given
			String notificationId = "notif-not-exist";
			given(dashboardService.markNotificationAsRead(notificationId, TEST_ADMIN_ID)).willReturn(false);

			// when
			ApiResponse<Map<String, Object>> response = controller.markNotificationAsRead(session, notificationId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("모든 알림 읽음 처리 API")
	class MarkAllNotificationsAsReadTest {

		@Test
		@DisplayName("모든 알림 읽음 처리 성공")
		void markAllNotificationsAsRead_Success() {
			// given
			given(dashboardService.markAllNotificationsAsRead(TEST_ADMIN_ID)).willReturn(5);

			// when
			ApiResponse<Map<String, Object>> response = controller.markAllNotificationsAsRead(session);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData().get("markCount")).isEqualTo(5);
		}

		@Test
		@DisplayName("모든 알림 읽음 처리 실패")
		void markAllNotificationsAsRead_Error() {
			// given
			given(dashboardService.markAllNotificationsAsRead(anyString())).willThrow(new RuntimeException("Error"));

			// when
			ApiResponse<Map<String, Object>> response = controller.markAllNotificationsAsRead(session);

			// then
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("전체 대시보드 데이터 조회 API")
	class GetAllDashboardDataTest {

		@Test
		@DisplayName("전체 데이터 조회 성공")
		void getAllDashboardData_Success() {
			// given
			DashboardStatsDto stats = DashboardStatsDto.builder()
				.dailyVisitors(100)
				.build();
			List<RecentActivityDto> activities = Collections.emptyList();
			List<AdminNotificationDto> notifications = Collections.emptyList();

			given(dashboardService.getStats(TEST_ADMIN_ID)).willReturn(stats);
			given(dashboardService.getRecentActivity(10)).willReturn(activities);
			given(dashboardService.getNotifications(TEST_ADMIN_ID, 10)).willReturn(notifications);

			// when
			ApiResponse<Map<String, Object>> response = controller.getAllDashboardData(session, 10, 10);

			// then
			assertThat(response).isNotNull();
			assertThat(response.isSuccess()).isTrue();
			assertThat(response.getData()).containsKeys("stats", "activities", "notifications");
		}

		@Test
		@DisplayName("전체 데이터 조회 실패")
		void getAllDashboardData_Error() {
			// given
			given(dashboardService.getStats(anyString())).willThrow(new RuntimeException("Error"));

			// when
			ApiResponse<Map<String, Object>> response = controller.getAllDashboardData(session, 10, 10);

			// then
			assertThat(response.isSuccess()).isFalse();
		}
	}

	@Nested
	@DisplayName("세션 관리")
	class SessionManagementTest {

		@Test
		@DisplayName("세션에 adminId 없을 때")
		void getStats_NoAdminIdInSession() {
			// given
			MockHttpSession emptySession = new MockHttpSession();
			DashboardStatsDto stats = DashboardStatsDto.builder().build();
			given(dashboardService.getStats(null)).willReturn(stats);

			// when
			ApiResponse<DashboardStatsDto> response = controller.getStats(emptySession);

			// then
			assertThat(response).isNotNull();
			// adminId가 null이어도 서비스 호출은 됨
			then(dashboardService).should().getStats(null);
		}
	}
}
