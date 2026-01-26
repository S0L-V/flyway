package com.flyway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.flyway.admin.interceptor.AdminAuthInterceptor;
import com.flyway.admin.interceptor.VisitorTrackingInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC 전역 설정
 * - Interceptor 등록
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final AdminAuthInterceptor adminAuthInterceptor;
	private final VisitorTrackingInterceptor visitorTrackingInterceptor;

	/**
	 * Interceptor 등록
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("--- WebMvcConfig.addInterceptors method called ---");
		log.info("VisitorTrackingInterceptor instance: {}", visitorTrackingInterceptor);
		// 관리자 인증 Interceptor
		registry.addInterceptor(adminAuthInterceptor)
			.addPathPatterns("/admin/**")
			.excludePathPatterns(
				"/admin/login",
				"/admin/api/auth/login",
				"/admin/api/auth/validate",
				"/admin/resources/**",
				"/resources/**"
			);

		// 방문자 추적 Interceptor (일반 사용자 페이지)
		registry.addInterceptor(visitorTrackingInterceptor)
			.addPathPatterns(
				"/",
				"/search", "/search/**",
				"/reservation", "/reservation/**",
				"/mypage, /mypage/**"
			)
			.excludePathPatterns(
				"/admin/**",
				"/api/**",
				"/resources/**"
			);
	}
}