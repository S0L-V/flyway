package com.flyway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 보안 설정
 * BCrypt 비밀번호 암호화 설정
 */
@Configuration
public class SecurityConfig {

	/**
	 * BCrypt 비밀번호 인코더 빈 등록
	 * 강도: 10 (기본값, 2^10 = 1024 해싱)
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}
}
