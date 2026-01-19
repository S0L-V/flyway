package com.flyway.admin.interceptor;

import static com.flyway.admin.util.IpUtil.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 인증 체크 Interceptor
 * - 모든 /admin/** 요청에 대해 세션 검증
 * - 로그인하지 않은 사용자 차단
 * - AJAX 요청과 일반 요청 구분 처리
 *
 * 제외 경로:
 * - /admin/login - 로그인 페이지
 * - /admin/api/auth/login - 로그인 API
 * - /admin/resources/** - 정적 리소스
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler) throws Exception {

		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();

		log.debug("Admin auth check - URI: {}", requestURI);

		// 세션 체크 (false: 세션 없으면 새로 생성 안 함)
		HttpSession session = request.getSession(false);

		// 세션이 없거나 adminId가 없으면 인증 실패
		if (session == null || session.getAttribute("adminId") == null) {
			log.warn("Unauthorized access attempt - URI: {}, IP: {}", requestURI, getClientIp(request));

			// AJAX 요청인지 확인
			String ajaxHeader = request.getHeader("X-Requested-With");
			boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

			if (isAjax) {
				// AJAX 요청 -> JSON 응답
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write(
					"{\"success\":false,\"message\":\"로그인이 필요합니다.\"}"
				);
			} else {
				// 일반 요청 -> 로그인 페이지로 리다이렉트
				String loginUrl = contextPath + "/admin/login";
				log.debug("Redirecting to login: {}", loginUrl);
				response.sendRedirect(loginUrl);
			}

			return false; // 컨트롤러 실행 중단
		}

		// 세션 활동 시간 업데이트
		session.setAttribute("lastAccessTime", System.currentTimeMillis());

		// 로그
		String adminId = (String)session.getAttribute("adminId");
		String role = (String)session.getAttribute("role");
		log.debug("Authorized - adminId: {}, role: {}, URI: {}", adminId, role, requestURI);

		return true; //컨트롤러 실행 계속
	}

}
