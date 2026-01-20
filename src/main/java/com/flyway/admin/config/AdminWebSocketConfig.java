package com.flyway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.flyway.admin.websocket.AdminDashboardWebSocketHandler;
import com.flyway.admin.websocket.AdminWebSocketInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 WebSocket 설정
 * /admin/ws/dashboard 엔드포인트 등록
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class AdminWebSocketConfig implements WebSocketConfigurer {

	private final AdminDashboardWebSocketHandler dashboardHandler;
	private final AdminWebSocketInterceptor webSocketInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// WebSocket 엔드포인트 등록
		registry.addHandler(dashboardHandler, "/admin/ws/dashboard")
			.addInterceptors(webSocketInterceptor)
			// SockJs 폴백 지원
			.withSockJS()
			// 클라이언트 라이브러리 URL (CDN 사용 시 필요없음)
			// .setClientLibraryUrl("")
			// Heartbeat 간격 (기본 25초)
			.setHeartbeatTime(25000)
			// 세션 쿠키 필요 (HTTP 세션 인증용)
			.setSessionCookieNeeded(true);

		// 순수 WebSocket (SockJS 없이)
		registry.addHandler(dashboardHandler, "/admin/ws/dashboard/raw")
			.addInterceptors(webSocketInterceptor)
			.setAllowedOriginPatterns("*");
	}
}
