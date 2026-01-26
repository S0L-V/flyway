package com.flyway.admin.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.flyway.admin.domain.Admin;
import com.flyway.admin.mapper.AdminMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 Repository 구현체
 * Mapper 호출, 비즈니스 로직 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

	private final AdminMapper adminMapper;

	@Override
	public Admin findByEmail(String email) {
		return adminMapper.findByEmail(email);
	}

	@Override
	public Admin findById(String adminId) {
		return adminMapper.findById(adminId);
	}

	@Override
	public void updateLoginInfo(String adminId, String ipAddress) {
		adminMapper.updateLoginInfo(adminId, ipAddress, LocalDateTime.now());
	}

	@Override
	public void handleLoginFailure(String adminId) {
		LocalDateTime lockedUntil = LocalDateTime.now().plusSeconds(30);

		// Atomic 업데이트 (단일 쿼리)
		adminMapper.handleLoginFailureAtomic(adminId, lockedUntil);
		
		// 로그 확인
		Admin admin = adminMapper.findById(adminId);
		int failedCount = admin.getFailedLoginCount() != null ? admin.getFailedLoginCount() : 0;

		log.debug("Login failed for admin: {}, failed count: {}", adminId, failedCount);

		if (failedCount >= 5) {
			log.warn("Account locked until {} for admin: {}", lockedUntil, adminId);
		} else {
			log.debug("Login failed for admin: {}, failed count: {}", adminId, failedCount);
		}
	}

	@Override
	public void handleLoginSuccess(String adminId) {
		// 실패 횟수 초기화 + 잠금 해제
		adminMapper.resetFailedCount(adminId);
		log.debug("Login success, reset failed count for admin: {}", adminId);
	}

	@Override
	public void save(Admin admin) {
		adminMapper.insert(admin);
		log.info("Admin created: {}", admin.getEmail());
	}

	@Override
	public void update(Admin admin) {
		adminMapper.update(admin);
		log.info("Admin updated: {}", admin.getEmail());
	}
}
