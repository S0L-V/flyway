package com.flyway.admin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import com.flyway.admin.dto.AdminUserDto;
import com.flyway.admin.mapper.AdminUserMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminUserRepositoryImpl implements AdminUserRepository {

	private final AdminUserMapper adminUserMapper;

	@Override
	public List<AdminUserDto> findUserList(String status, String searchKeyword, int offset, int limit) {
		return adminUserMapper.selectUserList(status, searchKeyword, offset, limit);
	}

	@Override
	public int countUsers(String status, String searchKeyword) {
		return adminUserMapper.countUsers(status, searchKeyword);
	}

	@Override
	public AdminUserDto findUserById(String userId) {
		return adminUserMapper.selectUserById(userId);
	}

	@Override
	public int updateUserStatus(String userId, String status) {
		return adminUserMapper.updateUserStatus(userId, status);
	}

	@Override
	public int countUsersByStatus(String status) {
		return adminUserMapper.countUsersByStatus(status);
	}
}
