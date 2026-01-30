package com.flyway.admin.repository;

import com.flyway.admin.dto.AdminNotificationDto;

public interface AdminNotificationRepository {

	int save(AdminNotificationDto notification);
}
