package com.flyway.admin.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 세션 관리자
 * 연결된 관리자 세션을 추적하고 메시지 브로드캐스트 담당
 */
@Component
@Slf4j
public class AdminWebSocketSessionManager {

	/**
	 * 연결된 세션 맵
	 * Key: WebSocket Session ID
	 * Value: WebSocketSession
	 */
	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	/**
	 * 세션-관리자 ID 매핑
	 * Key: WebSocket Session ID
	 * Value: Admin ID
	 */
	private final Map<String, String> sessionAdminMap = new ConcurrentHashMap<>();

	/**
	 * 세션 등록
	 * @param session WebSocket 세션
	 * @param adminId 관리자 ID
	 */
	public void addSession(WebSocketSession session, String adminId) {
		sessions.put(session.getId(), session);
		sessionAdminMap.put(session.getId(), adminId);
		log.info("WebSocket session registered: sessionId={}, adminId={}, totalSessions={}",
			session.getId(), adminId, sessions.size());
	}

	/**
	 * 세션 제거
	 * @param session WebSocket 세션
	 */
	public void removeSession(WebSocketSession session) {
		String adminId = sessionAdminMap.remove(session.getId());
		sessions.remove(session.getId());
		log.info("WebSocket session removed: sessionId={}, adminId={}, totalSessions={}",
			session.getId(), adminId, sessions.size());
	}

	/**
	 * 세션으로 관리자 ID 조회
	 * @param sessionId 세션 ID
	 * @return 관리자 ID
	 */
	public String getAdminId(String sessionId) {
		return sessionAdminMap.get(sessionId);
	}

	/**
	 * 모든 세션에 메시지 브로드캐스트
	 * @param message JSON 문자열 메시지
	 */
	public void broadcast(String message) {
		TextMessage textMessage = new TextMessage(message);
		int successCount = 0;
		int failCount = 0;

		for (WebSocketSession session : sessions.values()) {
			if (session.isOpen()) {
				try {
					synchronized (session) {
						session.sendMessage(textMessage);
					}
					successCount++;
				} catch (Exception e) {
					log.warn("Failed to send message to session: {}", session.getId());
					failCount++;
				}
			}
		}

		log.debug("Broadcast completed: success={}, fail={}", successCount, failCount);
	}

	/**
	 * 특정 관리자에게 메시지 전송
	 * @param adminId 관리자 ID
	 * @param message JSON 문자열 메시지
	 */
	public void sendToAdmin(String adminId, String message) {
		TextMessage textMessage = new TextMessage(message);

		sessionAdminMap.entrySet().stream()
			.filter(entry -> adminId.equals(entry.getValue()))
			.map(Map.Entry::getKey)
			.map(sessions::get)
			.filter(session -> session != null && session.isOpen())
			.forEach(session -> {
				try {
					synchronized (session) {
						session.sendMessage(textMessage);
					}
				} catch (Exception e) {
					log.warn("Failed to send message to admin: {}, session: {}",
						adminId, session.getId());
				}
			});
	}

	/**
	 * 특정 세션에 메시지 전송
	 * @param session WebSocket 세션
	 * @param message JSON 문자열 메시지
	 */
	public void sendToSession(WebSocketSession session, String message) {
		if (session != null && session.isOpen()) {
			try {
				synchronized (session) {
					session.sendMessage(new TextMessage(message));
				}
			} catch (Exception e) {
				log.warn("Failed to send message to session: {}", session.getId());
			}
		}
	}

	/**
	 * 연결된 세션 수
	 */
	public int getSessionCount() {
		return sessions.size();
	}

	/**
	 * 세션이 존재하는지 확인
	 */
	public boolean hasActiveSessions() {
		return !sessions.isEmpty();
	}
}
