package com.flyway.admin.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.domain.Admin;
import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;
import com.flyway.admin.repository.AdminRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.admin.security.AdminJwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthServiceImpl implements AdminAuthService {

	private final AdminRepository adminRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AdminJwtUtil adminJwtUtil;

	/**
	 * 로그인 처리
	 * @param req 로그인 요청 (이메일, 비밀번호)
	 * @param ipAddress 클라이언트 IP
	 * @return LoginResponse (JWT 토큰 포함)
	 */
	@Override
	@Transactional(noRollbackFor = BusinessException.class)
	public LoginResponse login(LoginRequest req, String ipAddress) {

		// 1. 이메일로 관리자 조회
		Admin admin = adminRepository.findByEmail(req.getEmail());
		if (admin == null) {
			log.warn("Login failed - admin not found: {}", req.getEmail());
			throw new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
		}

		// 2. 계정 잠금 확인
		if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
			log.warn("Login failed - account locked: {}, until: {}", admin.getEmail(), admin.getLockedUntil());

			throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_LOCKED,
				String.format("계정이 잠겼습니다. %s 이후 다시 시도해주세요.", admin.getLockedUntil()));
		}

		// 3. 계정 활성화 확인
		if (!"Y".equals(admin.getIsActive())) {
			log.warn("Login failed - inactive account: {}", admin.getEmail());
			throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE);
		}

		// 4. 비밀번호 검증
		if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
			log.warn("Login failed - invalid password: {}, IP: {}", admin.getEmail(), ipAddress);

			// 로그인 실패 처리 (실패 횟수 증가 + 5회 시 잠금)
			adminRepository.handleLoginFailure(admin.getAdminId());

			throw new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
		}

		// 5. 로그인 성공 처리
		adminRepository.handleLoginSuccess(admin.getAdminId());
		adminRepository.updateLoginInfo(admin.getAdminId(), ipAddress);

		log.info("Login success: {}, IP: {}", admin.getEmail(), ipAddress);


		// 6. JWT 토큰 생성
		String accessToken = adminJwtUtil.generateToken(
			admin.getAdminId(),
			admin.getEmail(),
			admin.getRole().name()
		);

		// 7. 응답 생성
		return LoginResponse.builder()
			.adminId(admin.getAdminId())
			.adminName(admin.getAdminName())
			.email(admin.getEmail())
			.role(admin.getRole())
			.accessToken(accessToken)
			.lastLoginAt(LocalDateTime.now())
			.build();
	}

	/**
	 * 로그아웃 처리
	 * (JWT stateless 로그 기록용)
	 * @param adminId
	 */
	@Override
	public void logout(String adminId) {
		log.info("Logout: adminId={}", adminId);
		// 활동 로그 추가
	}

	@Override
	public Admin getAdminByEmail(String email) {
		return adminRepository.findByEmail(email);
	}
}
