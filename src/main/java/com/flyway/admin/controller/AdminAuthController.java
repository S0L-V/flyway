package com.flyway.admin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	/**
	 * 관리자 로그인
	 */
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

	/**
	 * 관리자 로그아웃
	 */
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
		// 세션에서 관리자 ID 가져오기
		String adminId = (String)session.getAttribute("adminId");

		if (adminId != null) {
			log.info("Logout: adminId={}", adminId);
			adminAuthService.logout(adminId);
		}

		// 세션 무효화
		session.invalidate();

		return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
	}

	/**
	 * 토큰 검증
	 * 프론트엔드에서 토큰 유효성 확인용
	 */
	@GetMapping("/validate")
	public ResponseEntity<ApiResponse<Boolean>> validateToken(HttpSession session) {
		String token = (String)session.getAttribute("adminToken");

		if (token == null) {
			return ResponseEntity.ok(ApiResponse.success(false, "토큰이 없습니다."));
		}

		return ResponseEntity.ok(ApiResponse.success(true, "유효한 토큰입니다."));
 	}
}
