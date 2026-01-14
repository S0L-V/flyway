package com.flyway.admin.service;

import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;

/**
 * 관리자 인증 인터페이스
 * 로그인/로그아웃 처리
 */
public interface AdminAuthService {

	LoginResponse login(LoginRequest req, String ipAddress);

	void logout(String adminId);
}
