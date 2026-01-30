package com.flyway.admin.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.flyway.admin.dto.AdminNotificationDto;

@Mapper
public interface AdminNotificationMapper {

	/**
	 * 새로운 알림을 DB에 저장
	 */
	int insertNotification(AdminNotificationDto notification);
}
