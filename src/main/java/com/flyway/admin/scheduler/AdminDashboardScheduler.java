package com.flyway.admin.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.dto.WebSocketMessageDto;
import com.flyway.admin.service.AdminDashboardService;
import com.flyway.admin.websocket.AdminWebSocketSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 대시보드 스케줄러
 * 주기적으로 실시간 데이터를 WebSocket으로 브로드케스트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardScheduler {

	private final AdminWebSocketSessionManager sessionManager;
	private final AdminDashboardService dashboardService;
	private final ObjectMapper objectMapper;

	@Scheduled(fixedRate = 5000)
	public void broadcastStats() {
		if (!sessionManager.hasActiveSessions()) {
			return;
		}

		try {
			// 통계 조회 (adminId는 null로 전달 - 개별 알림 수는 제외)
			DashboardStatsDto stats = dashboardService.getStats(null);
			String json = objectMapper.writeValueAsString(WebSocketMessageDto.stats(stats));
			sessionManager.broadcast(json);

			log.debug("Stats broadcasted to {} sessions", sessionManager.getSessionCount());

		} catch (Exception e) {
			log.error("Failed to broadcast stats", e);
		}
	}

	/**
	 * 최근 활동 브로드캐스트 (10초마다)
	 * 연결된 세션이 있을 때만 실행
	 */
	@Scheduled(fixedRate = 10000)
	public void broadcastActivities() {
		if (!sessionManager.hasActiveSessions()) {
			return;
		}

		try {
			List<RecentActivityDto> activities = dashboardService.getRecentActivity(10);
			String json = objectMapper.writeValueAsString(WebSocketMessageDto.activities(activities));
			sessionManager.broadcast(json);

			log.debug("Activities broadcasted to {} sessions", sessionManager.getSessionCount());

		} catch (Exception e) {
			log.error("Failed to broadcast activities", e);
		}
	}

	/**
	 * 연결 상태 로깅 (1분마다)
	 */
	@Scheduled(fixedRate = 60000)
	public void logConnectionStatus() {
		int sessionCount = sessionManager.getSessionCount();
		if (sessionCount > 0) {
			log.info("WebSocket active sessions: {}", sessionCount);
		}
	}
}
