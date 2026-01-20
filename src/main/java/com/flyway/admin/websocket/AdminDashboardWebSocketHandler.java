package com.flyway.admin.websocket;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AdminDashboardWebSocketHandler {

	private final AdminWebSocketSessionManager sessionManager;
	private final AdminDashboardService dashboardService;
	private final ObjectMapper objectMapper;


}
