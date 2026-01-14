package com.flyway.admin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;
import com.flyway.admin.service.AdminAuthService;
import com.flyway.template.common.ApiResponse;
import com.flyway.util.IpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 인증 인터셉터
 * 로그인/로그아웃 API
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/auth")
@RequiredArgsConstructor
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
		@RequestBody LoginRequest req,
		HttpServletRequest httpRequest,
		HttpSession session) {

		log.info("Login attempt: email={}", req.getEmail());

		// 클라이언트 IP 추출
		String clientIp = IpUtil.getClientIp(httpRequest);
		log.debug("Client IP: {}", clientIp);

		// 로그인 처리
		LoginResponse res = adminAuthService.login(req, clientIp);

		// 세션에 JWT 및 관리자 정보 저장
		session.setAttribute("adminToken", res.getAccessToken());
		session.setAttribute("adminId", res.getAdminId());
		session.setAttribute("adminName", res.getAdminName());
		session.setAttribute("adminRole", res.getRole().name());

		// 세션 타임아웃 설정 (30분)
		session.setMaxInactiveInterval(1800);

		log.info("Login success: adminId={}, role={}", res.getAdminId(), res.getRole());

		return ResponseEntity.ok(ApiResponse.success(res, "로그인 성공"));
	}
}
