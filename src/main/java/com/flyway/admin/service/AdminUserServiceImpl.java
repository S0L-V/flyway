package com.flyway.admin.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.dto.AdminUserDto;
import com.flyway.admin.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

	private final AdminUserRepository adminUserRepository;

	@Override
	@Transactional(readOnly = true)
	public List<AdminUserDto> getUserList(String status, String searchKeyword, int page, int size) {
		try {
			int safePage = Math.max(1, page);
			int safeSize = Math.max(1, Math.min(size, 100));
			int offset = (safePage - 1) * safeSize;
			return adminUserRepository.findUserList(status, searchKeyword, offset, safeSize);
		} catch (Exception e) {
			log.error("Failed to get user list - status: {}, keyword: {}", status, searchKeyword, e);
			return Collections.emptyList();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getUserCount(String status, String searchKeyword) {
		try {
			return adminUserRepository.countUsers(status, searchKeyword);
		} catch (Exception e) {
			log.error("Failed to count users - status: {}, keyword: {}", status, searchKeyword, e);
			return 0;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AdminUserDto getUserById(String userId) {
		try {
			return adminUserRepository.findUserById(userId);
		} catch (Exception e) {
			log.error("Failed to get user by id: {}", userId);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updateUserStatus(String userId, String status) {
		try {
			if (!isValidStatus(status)) {
				log.warn("Invalid status: {}", status);
				return false;
			}

			int result = adminUserRepository.updateUserStatus(userId, status);
			if (result > 0) {
				log.info("User status updated - userId: {}, status: {}", userId, status);
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Failed to update user status - userId: {}, status: {}", userId, status, e);
			return false;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Integer> getUserStats() {
		try {
			Map<String, Integer> stats = new HashMap<>();
			stats.put("active", adminUserRepository.countUsersByStatus("ACTIVE"));
			stats.put("blocked", adminUserRepository.countUsersByStatus("BLOCKED"));
			stats.put("onboarding", adminUserRepository.countUsersByStatus("ONBOARDING"));
			stats.put("withdrawn", adminUserRepository.countUsersByStatus("WITHDRAWN"));
			stats.put("total", stats.values().stream().mapToInt(Integer::intValue).sum());
			return stats;
		} catch (Exception e) {
			log.error("Failed to get user stats", e);
			return Collections.emptyMap();
		}
	}

	private boolean isValidStatus(String status) {
		return "ACTIVE".equals(status) || "BLOCKED".equals(status);
	}

}
