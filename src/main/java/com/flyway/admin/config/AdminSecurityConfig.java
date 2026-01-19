package com.flyway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 관리자 전용 Spring Security 설정
 * /admin/** 경로만 담당
 * Spring Security FilterChain 분리
 * 세션 기반 인증 (Interceptor에서 체크)
 *
 * @Order(2) API(1) 다음, 일반 사용자(3) 이전 처리
 */
@Configuration
@EnableWebSecurity
@Order(3)
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
			// /admin/** 경로만 이 설정이 처리
			.antMatcher("/admin/**")
			// CSRF 비활성화 (AJAX 사용)
			.csrf().disable()
			// 권한 설정
			.authorizeRequests()
				// 로그인 페이지는 누구나 접근 가능
				.antMatchers("/admin/login").permitAll()

				// 로그인 API는 누구나 접근 가능
				.antMatchers("admin/api/auth/login").permitAll()

				// JWT 검증 API는 누구나 접근 가능 (나중에 사용)
				.antMatchers("/admin/api/auth/validate").permitAll()

				// 정적 리소스는 누구나 접근 가능
				.antMatchers("admin/resources/**").permitAll()
				.antMatchers("/resources/**").permitAll()

				// 나머지 permitAll (Interceptor 에서 세션 체크)
				.anyRequest().permitAll()
			.and()

			// 기본 로그인 폼 비활성화 (직접 구현)
			.formLogin().disable()

			// HTTP Basic 인증 비활성화
			.httpBasic().disable()

			// 로그아웃 설정
			.logout()
				.logoutUrl("/admin/logout")
				.logoutSuccessUrl("/admin/login")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll();
	}
}
