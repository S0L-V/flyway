package com.flyway.admin.interceptor;

import static com.flyway.admin.util.IpUtil.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.flyway.admin.events.VisitorEvent;
import com.flyway.admin.service.VisitorLogQueryService;
import com.flyway.security.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 방문자 추적 Interceptor
 * - 사이트 방문 기록을 visitor_log 테이블에 저장
 * - 세션 기준으로 일일 1회만 기록 (중복 방지)
 * - 비회원/회원 모두 추적
 *
 * 대상 경로:
 * - / (메인 페이지)
 * - /search/** (항공편 검색/조회)
 * - /reservation/** (예약 관련)
 * - /mypage/** (마이페이지)
 *
 * 제왹 경로:
 * - /admin/** (관리자 페이지)
 * - /resources/** (정적 리소스)
 * - /api/** (API 요청)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorTrackingInterceptor implements HandlerInterceptor {

	private final ApplicationEventPublisher eventPublisher;
	private final VisitorLogQueryService visitorLogQueryService;

	private static final String VISITOR_TRACKED_KEY = "visitorTracked";

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler) throws Exception {

		try {
			// 세션 가져오기
			log.info("--- VisitorTrackingInterceptor PREHANDLE START for path: {}", request.getRequestURI());
			HttpSession session = request.getSession(true);
			String sessionId = session.getId();

			// 이번 세션에서 이미 추적했는지 확인 (메모리 캐시)
			Boolean alreadyTracked = (Boolean)session.getAttribute(VISITOR_TRACKED_KEY);
			if (Boolean.TRUE.equals(alreadyTracked)) {
				return true; // 이미 추적됨, 스킵
			}

			// DB에서 오늘 이미 기록되었는지 확인
			if (visitorLogQueryService.existsToday(sessionId)) {
				// DB에 이미 있으면 세션에 마킹하고 스킵
				session.setAttribute(VISITOR_TRACKED_KEY, true);
				return true;
			}

			// 방문 로그 저장
			String userId = getUserIdFromSession(session); // 로그인 사용자인 경우
			String ipAddress = getClientIp(request);
			String userAgent = request.getHeader("User-Agent");
			String pageUrl = request.getRequestURI();
			String referer = request.getHeader("Referer");

			// User-Agent 길이 제한 (DB 칼럼 500)
			if (userAgent != null && userAgent.length() > 500) {
				userAgent = userAgent.substring(0, 500);
			}

			// Page URL 길이 제한 (DB 칼럼 500)
			if (pageUrl != null && pageUrl.length() > 500) {
				pageUrl = pageUrl.substring(0, 500);
			}

			// Referer 길이 제한 (DB 칼럼 500)
			if (referer != null && referer.length() > 500) {
				referer = referer.substring(0, 500);
			}

			// 진단용 로그
			log.info("Visitor tracking data collected. userId: {}, pageUrl: {}, referer: {}", userId, pageUrl, referer);

			// 이벤트 발생
			VisitorEvent event = new VisitorEvent(this, sessionId, userId, ipAddress,
				userAgent, pageUrl, referer);
			eventPublisher.publishEvent(event);

			session.setAttribute(VISITOR_TRACKED_KEY, true);

			log.debug("Visitor tracked - sessionId: {}, ip:{}, url:{}", sessionId, ipAddress, pageUrl);
		} catch (Exception e) {
			// 방문 추적 실패해도 요청은 계속 처리
			// log.warn("Failed to track visitor: {}", e.getMessage());
			log.error("Failed to track visitor", e);
		}

		return true; // 항상 요청 계속 처리
	}

	public String getUserIdFromSession(HttpSession session) {
		SecurityContext securityContext = (SecurityContext)session.getAttribute(
			HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		if (securityContext == null) {
			return null;
		}

		Authentication authentication = securityContext.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(
			authentication.getPrincipal().toString())) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomUserDetails) {
			return ((CustomUserDetails)principal).getUserId();
		}

		return null;
	}
}
