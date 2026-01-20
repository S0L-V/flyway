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

	private long notificationId; // 알림 ID
	private String adminId;
	private String type; // 알림 유형 (INFO, WARNING, ERROR, SUCCESS)
	private String title;
	private String message;
	private String linkUrl; // 클릭 시 이동 URL (nullable)
	private String isRead;
	private LocalDateTime readAt;
	private LocalDateTime createdAt;
}
