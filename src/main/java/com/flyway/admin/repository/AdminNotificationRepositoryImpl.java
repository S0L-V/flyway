package com.flyway.admin.repository;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.AdminNotificationDto;
import com.flyway.admin.mapper.AdminNotificationMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminNotificationRepositoryImpl implements AdminNotificationRepository {

	private final AdminNotificationMapper adminNotificationMapper;

	@Override
	public int save(AdminNotificationDto notification) {
		return adminNotificationMapper.insertNotification(notification);
	}
}
