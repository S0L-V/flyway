package com.flyway.admin.websocket;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket Handshake 인터셉터
 * HTTP 세션 기반 인증 체크
 */
@Component
@Slf4j
public class AdminWebSocketInterceptor implements HandshakeInterceptor {

	private static final String ADMIN_ID_ATTR = "adminId";
	private static final String ADMIN_NAME_ATTR = "adminName";
	private static final String ADMIN_ROLE_ATTR = "adminRole";

	/**
	 * Handshake 전 인증 체크
	 * HTTP 세션에서 adminId를 확인하여 인증된 관리자만 연결 허용
	 */
	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {

		log.debug("WebSocket handshake attempt");

		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest)request;
			HttpSession session = servletRequest.getServletRequest().getSession(false);

			if (session != null) {
				String adminId = (String)session.getAttribute(ADMIN_ID_ATTR);
				String adminName = (String)session.getAttribute(ADMIN_NAME_ATTR);
				String adminRole = (String)session.getAttribute(ADMIN_ROLE_ATTR);

				if (adminId != null) {
					// WebSocket 세션 속성에 관리자 정보 저장
					// ConcurrentHashMap은 null 값 허용 안 함 - null 체크 필수
					attributes.put(ADMIN_ID_ATTR, adminId);
					if (adminName != null) {
						attributes.put(ADMIN_NAME_ATTR, adminName);
					}
					if (adminRole != null) {
						attributes.put(ADMIN_ROLE_ATTR, adminRole);
					}

					log.info("WebSocket handshake authorized: adminId={}, adminName={}",
						adminId, adminName);
					return true;
				}
			}
		}

		log.warn("WebSocket handshake rejected: no valid session");
		return false;
	}

	/**
	 * Handshake 후 처리
	 */
	@Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception) {

		if (exception != null) {
			log.error("WebSocket handshake failed", exception);
		}
	}
}
