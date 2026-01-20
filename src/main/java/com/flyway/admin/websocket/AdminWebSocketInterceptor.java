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
	private static final String ADMIN_EMAIL_ATTR = "email";
	private static final String ADMIN_ROLE_ATTR = "role";

	/**
	 * Handshake 전 인증 체크
	 * HTTP 세션에서 adminId를 확인하여 인증된 관리자만 연결 허용
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {

		log.debug("WebSocket handshake attempt");

		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest)request;
			HttpSession session = servletRequest.getServletRequest().getSession(false);

			if (session != null) {
				String adminId = (String)session.getAttribute(ADMIN_ID_ATTR);
				String email = (String)session.getAttribute(ADMIN_EMAIL_ATTR);
				String role = (String)session.getAttribute(ADMIN_ROLE_ATTR);

				if (adminId != null) {
					// WebSocket 세션 속성에 관리자 정보 저장
					attributes.put(ADMIN_ID_ATTR, adminId);
					attributes.put(ADMIN_EMAIL_ATTR, email);
					attributes.put(ADMIN_ROLE_ATTR, role);

					log.info("WebSocket handshake authorized: adminId={}, email={}",
						adminId, email);
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
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

		if (exception != null) {
			log.error("WebSocket handshake failed", exception);
		}
	}
}
