package com.flyway.admin.service;

import com.flyway.admin.dto.AdminNotificationDto;

public interface AdminNotificationService {

	/**
	 * 알림을 생성하고, DB에 저장한 후, 모든 관리자에게 실시간으로 알림을 전송함
	 */
	void createAndBroadcastNotification(AdminNotificationDto notification);
}
