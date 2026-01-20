package com.flyway.admin.websocket;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.DashboardStatsDto;
import com.flyway.admin.dto.RecentActivityDto;
import com.flyway.admin.dto.WebSocketMessageDto;
import com.flyway.admin.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 대시보드 WebSocket 핸들러
 * 실시간 통계 및 알림 전송
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardWebSocketHandler extends TextWebSocketHandler {

	private final AdminWebSocketSessionManager sessionManager;
	private final AdminDashboardService dashboardService;
	private final ObjectMapper objectMapper;

	private static final String ADMIN_ID_ATTR = "adminId";

	/**
	 * 연결 성공 시 호출
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String adminId = getAdminId(session);

		if (adminId == null) {
			log.warn("WebSocket connection rejected: no adminId in session attributes");
			session.close(CloseStatus.NOT_ACCEPTABLE);
			return;
		}

		// 세션 등록
		sessionManager.addSession(session, adminId);

		// 초기 데이터 전송
		sendInitialDate(session, adminId);

		log.info("WebSocket connected: sessionId={}, adminId={}", session.getId(), adminId);
	}

	/**
	 * 메시지 수신 시 호출
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		String adminId = getAdminId(session);

		log.debug("WebSocket message received: sessionId={}, payload={}", session.getId(), payload);

		try {
			// 간단한 메시지 파싱 (type만 확인)
			if (payload.contains("\"type\":\"REQUEST_STATS\"")) {
				// 통계 요청 -> 즉시 응답
				sendStats(session, adminId);
			} else if (payload.contains("\"type\":\"REQUEST_ACTIVITIES\"")) {
				// 활동 요청 -> 즉시 응답
				sendActivities(session);
			} else if (payload.contains("\"type\":\"REQEUST_NOTIFICATIONS\"")) {
				// 알림 요청 -> 즉시 응답
				sendNotifications(session, adminId);
			}

		} catch (Exception e) {
			log.error("Failed to handle WebSocket message", e);
			String errorMessage = objectMapper.writeValueAsString(
				WebSocketMessageDto.error("메시지 처리 중 오류가 발생했습니다."));

			sessionManager.sendToSession(session, errorMessage);
		}
	}

	/**
	 * 연결 종료 시 호출
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessionManager.removeSession(session);
		log.info("WebSocket disconnected: sessionId={}, status={}", session.getId(), status);
	}

	/**
	 * 에러 발생 시 호출
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket transport error: sessionId={}", session.getId(), exception);
		sessionManager.removeSession(session);
	}

	/**
	 * 초기 데이터 전송
	 */
	private void sendInitialDate(WebSocketSession session, String adminId) {
		try {
			// 통계
			sendStats(session, adminId);

			// 최근 활동
			sendActivities(session);

			// 알림
			sendNotifications(session, adminId);
		} catch (Exception e) {
			log.error("Failed to send initial data", e);
		}
	}

	/**
	 * 통계 전송
	 */
	private void sendStats(WebSocketSession session, String adminId) throws Exception {
		DashboardStatsDto stats = dashboardService.getStats(adminId);
		String json = objectMapper.writeValueAsString(WebSocketMessageDto.stats(stats));
		sessionManager.sendToSession(session, json);
	}

	/**
	 * 최근 활동 전송
	 */
	private void sendActivities(WebSocketSession session) throws Exception {
		List<RecentActivityDto> activities = dashboardService.getRecentActivity(10);
		String json = objectMapper.writeValueAsString(WebSocketMessageDto.activities(activities));
		sessionManager.sendToSession(session, json);
	}

	/**
	 * 알림 전송
	 */
	private void sendNotifications(WebSocketSession session, String adminId) throws Exception {
		List<AdminNotificationDto> notifications = dashboardService.getNotifications(adminId, 10);
		String json = objectMapper.writeValueAsString(WebSocketMessageDto.notifications(notifications));
		sessionManager.sendToSession(session, json);
	}

	/**
	 * 세션에서 관리자 ID 추출
	 */
	private String getAdminId(WebSocketSession session) {
		Object adminId = session.getAttributes().get(ADMIN_ID_ATTR);
		return adminId != null ? adminId.toString() : null;
	}

}
