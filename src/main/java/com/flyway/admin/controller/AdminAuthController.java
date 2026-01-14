package com.flyway.admin.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.bytebuddy.matcher.StringMatcher;

import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;
import com.flyway.admin.service.AdminAuthService;
import com.flyway.template.exception.BusinessException;
import com.flyway.util.IpUtil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 인증 인터셉터
 * 로그인/로그아웃 API
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	/**
	 * 로그인 페이지
	 * GET /admin/login
	 */
	@GetMapping("/admin/login")
	public String loginPage(HttpSession session) {
		if (session.getAttribute("adminId") != null) {
			return "redirect:/admin/dashboard";
		}
		return "admin/login";
	}

	@GetMapping("/admin/dashboard")
	public String dashboardPage(HttpSession session, Model model) {
		// 세션 확인
		String adminId = (String)session.getAttribute("adminId");
		if (adminId == null) {
			return "redirect:/admin/login";
		}

		model.addAttribute("adminName", session.getAttribute("adminName"));
		model.addAttribute("adminRole", session.getAttribute("adminRole"));

		return "admin/dashboard";
	}


	/**
	 * 로그인 처리
	 * POST /admin/api/auth/login
	 */
	@PostMapping("/admin/api/auth/login")
	@ResponseBody
	public Map<String, Object> login(
		@RequestBody LoginRequest loginRequest,
		HttpServletRequest httpRequest,
		HttpSession session) {

		log.info("Login attempt: email={}", loginRequest.getEmail());

		try {
			// 클라이언트 IP 추출
			String clientIp = IpUtil.getClientIp(httpRequest);
			log.debug("Client IP: {}", clientIp);

			// 로그인 처리
			LoginResponse loginResponse = adminAuthService.login(loginRequest, clientIp);

			// 세션에 JWT 및 관리자 정보 저장
			session.setAttribute("adminToken", loginResponse.getAccessToken());
			session.setAttribute("adminId", loginResponse.getAdminId());
			session.setAttribute("adminName", loginResponse.getAdminName());
			session.setAttribute("adminRole", loginResponse.getRole().name());

			// 세션 타임아웃 설정 (30분)
			session.setMaxInactiveInterval(1800);

			log.info("Login success: adminId={}, role={}", loginResponse.getAdminId(), loginResponse.getRole());

			// 성공 응답
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", loginResponse);
			response.put("message", "로그인 성공");
			return response;
		} catch (BusinessException e) {
			log.error("Login failed: {}", e.getMessage());

			// 실패 응답
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", e.getMessage());
			response.put("errorCode", e.getErrorCode().getCode());
			return response;
		} catch (Exception e) {
			log.error("Unexpected error during login", e);

			// 예상치 못한 에러
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "서버 오류가 발생했습니다.");
			response.put("errorCode", "C002");
			return response;
		}
	}

	/**
	 * 로그아웃
	 * POST /admin/logout
	 */
	@PostMapping("/admin/logout")
	public String logout(HttpSession session) {
		// 세션에서 관리자 ID 가져오기
		String adminId = (String)session.getAttribute("adminId");

		if (adminId != null) {
			log.info("Logout: adminId={}", adminId);
			adminAuthService.logout(adminId);
		}

		// 세션 무효화
		session.invalidate();

		return "redirect:/admin/login";
	}

	/**
	 * 토큰 검증 (AJAX)
	 * GET /admin/api/auth/validate
	 */
	@GetMapping("/admin/api/auth/validate")
	public Map<String, Object> validateToken(HttpSession session) {
		String token = (String)session.getAttribute("adminToken");

		Map<String, Object> response = new HashMap<>();

		if (token == null) {
			response.put("success", false);
			response.put("message", "토큰이 없습니다.");
		} else {
			response.put("success", true);
			response.put("message", "유효한 토큰입니다.");
		}

		return response;
 	}
}
