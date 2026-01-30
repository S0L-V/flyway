package com.flyway.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.WebSocketMessageDto;
import com.flyway.admin.repository.AdminNotificationRepository;
import com.flyway.admin.websocket.AdminWebSocketSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationServiceImpl implements AdminNotificationService {

	private final AdminNotificationRepository adminNotificationRepository;
	private final AdminWebSocketSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public void createAndBroadcastNotification(AdminNotificationDto notification) {
		try {
			// 1. 알림 객체 준비 (ID 및 기본값 설정)
			notification.setNotificationId(UUID.randomUUID().toString());
			if (notification.getPriority() == null) {
				notification.setPriority("NORMAL");
			}

			// 2. DB에 알림 저장
			adminNotificationRepository.save(notification);
			log.info("Notification created: type={}, resourceId={}", notification.getNotificationType(),
				notification.getRelatedResourceType());

			// 3. 실시간 알림 전송 (새로고침 메시지)
			if (sessionManager.hasActiveSessions()) {
				// 모든 클라이언트에게 통계, 활동, 알림 목록을 새로고침하는 메시지를 보냄
				WebSocketMessageDto<Void> refreshMessage = WebSocketMessageDto.refreshAll();
				String messagePayload = objectMapper.writeValueAsString(refreshMessage);
				sessionManager.broadcast(messagePayload);
				log.info("Broadcasted REFRESH_ALL message to all admin clients.");
			}
		} catch (Exception e) {
			log.error("Failed to create and broad cast notification: {}", notification, e);
			// 알림 생성 실패가 주요 로직에 영향을 주지 않도록 예외를 다시 던지지 않음
		}
	}
}
