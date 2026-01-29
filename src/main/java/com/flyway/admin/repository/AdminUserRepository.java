package com.flyway.admin.repository;

import java.util.List;

import com.flyway.admin.dto.AdminUserDto;

public interface AdminUserRepository {

	List<AdminUserDto> findUserList(String status, String searchKeyword, int offset, int limit);

	int countUsers(String status, String searchKeyword);

	AdminUserDto findUserById(String userId);

	int updateUserStatus(String userId, String status);

	int countUsersByStatus(String status);
}
