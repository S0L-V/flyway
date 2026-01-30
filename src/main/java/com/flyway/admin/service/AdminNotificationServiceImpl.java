package com.flyway.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.dto.WebSocketMessageDto;
import com.flyway.admin.repository.AdminNotificationRepository;
import com.flyway.admin.websocket.AdminWebSocketSessionManager;
import com.flyway.seat.dto.SeatReleaseResponse;

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

			// 3. 트랜잭션 커밋 후 WebSocket 브로드캐스트 (롤백 시 전송 안 함)
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						broadcastRefreshMessage();
					}
				});
			} else {
				// 트랜잭션이 없으면 즉시 전송
				broadcastRefreshMessage();
			}
		} catch (Exception e) {
			log.error("Failed to create and broad cast notification: {}", notification, e);
			// 알림 생성 실패가 주요 로직에 영향을 주지 않도록 예외를 다시 던지지 않음
		}
	}

	private void broadcastRefreshMessage() {
		try {
			if (sessionManager.hasActiveSessions()) {
				WebSocketMessageDto<Void> refreshMessage = WebSocketMessageDto.refreshAll();
				String messagePayload = objectMapper.writeValueAsString(refreshMessage);
				sessionManager.broadcast(messagePayload);
				log.info("Broadcasted REFRESH_ALL message to all admin clients.");
			}
		} catch (Exception e) {
			log.error("Failed to broadcast refresh message", e);
		}
	}
}
