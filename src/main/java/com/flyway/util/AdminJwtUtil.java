package com.flyway.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 관리자용 JWT 유틸리티
 * 일반 사용자와 완전히 분리된 토큰
 */
@Component
public class AdminJwtUtil {

	@Value("${jwt.admin.secret}")
	private String adminSecretKey;

	@Value("${jwt.admin.expiration}")
	private long adminExpiration;

	/**
	 * 서명 키 생성
	 */
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(adminSecretKey.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 관리자용 JWT 토큰 생성
	 */
	public String generateToken(String adminId, String email, String role) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + adminExpiration);

		return Jwts.builder()
			.subject(adminId)
			.claim("email", email)
			.claim("role", role)
			.claim("type", "ADMIN")
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	/**
	 * 토큰에서 관리자 ID 추출
	 */
	public String getAdminIdFromToken(String token) {
		Claims claims = getClaims(token);
		return claims.getSubject();
	}

	/**
	 * 토큰에서 권한 추출
	 */
	public String getRoleFromToken(String token) {
		Claims claims = getClaims(token);
		return claims.get("role", String.class);
	}

	/**
	 * 토큰에서 이메일 추출
	 */
	public String getEmailFromToken(String token) {
		Claims claims = getClaims(token);
		return claims.get("email", String.class);
	}

	/**
	 * 토큰 유효성 검증
	 */
	public boolean validateToken(String token) {
		try {
			Claims claims = getClaims(token);

			// 토큰 타입 확인
			String type = claims.get("type", String.class);
			if (!"ADMIN".equals(type)) {
				return false;
			}

			// 만료 확인
			return !claims.getExpiration().before(new Date());
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	/**
	 * Claims 추출
	 */
	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
