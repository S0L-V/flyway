package com.flyway.admin.dto;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 알림 DTO
 * admin_notification 테이블 매핑
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationDto {

	private String notificationId; // 알림 ID
	private String adminId;
	private String notificationType; // 알림 유형 (NEW_RESERVATION, PAYMENT_FAILED, SYSTEM_ALERT)
	private String title;
	private String message;
	private String relatedResourceType; // 관련 리소스 타입 (RESERVATION, REFUND, PAYMENT)
	private String relatedResourceId; // 관련 리소스 ID
	private String priority; // 우선순위 (HIGH, NORMAL, LOW)
	private String isRead; // 읽음 여부 (Y/N)
	private LocalDateTime readAt;
	private LocalDateTime createdAt;
}
