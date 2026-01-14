package com.flyway.util;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * IP 주소 추출 유틸리티
 */
@Slf4j
public class IpUtil {

	/**
	 * 실제 클라이언트 IP 주소 가져오기
	 * nginx 프록시 환경을 고려
	 */
	public static String getClientIp(HttpServletRequest req) {
		if (req == null) {
			log.warn("HttpServletRequest is null");
			return "UNKNOWN";
		}

		String ip = null;

		// 1. X-Forwarded-For 헤더 확인 (nginx 표준)
		ip = req.getHeader("X-Forwarded-For");
		if (isValidIp(ip)) {
			// 여러 IP가 있을 경우 첫 번째가 실제 클라이언트 IP
			// 예: "123.45.67.89, 10.0.0.1" -> "123.45.67.89"
			String clientIp = ip.split(",")[0].trim();
			if (isValidIp(clientIp)) {
				log.debug("Client IP from X-Forwarded-For: {}", clientIp);
				return clientIp;
			}
		}

		// 2. X-Real-IP 헤더 (nginx)
		ip = req.getHeader("X-Real-IP");
		if (isValidIp(ip)) {
			log.debug("Client IP from X-Real-IP: {}", ip);
			return ip;
		}

		// 8. REMOTE_ADDR (직접 연결 또는 로컬 개발)
		ip = req.getRemoteAddr();
		if (isValidIp(ip)) {
			// 로컬 개발 환경 처리
			if ("0:0:0:0:0:0:0:1".equals(ip)) {
				log.debug("IPv6 localhost detected, converting to IPv4");
				return "127.0.0.1";
			}
			log.debug("Client IP from RemoteAddr: {}", ip);
			return ip;
		}

		log.warn("Could not determine client IP, returning UNKNOWN");
		return "UNKNOWN";
	}

	/**
	 * IP 주소 유효성 검증
	 */
	private static boolean isValidIp(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}

		// "unknown" 문자열 체크 (일부 프록시가 설정 안된 경우)
		if ("unknown".equalsIgnoreCase(ip)) {
			return false;
		}

		return true;
	}

	/**
	 * IPv6 여부 확인
	 */
	public static boolean isIPv6(String ip) {
		return ip != null && ip.contains(":");
	}

	/**
	 * 로컬 IP 여부 확인
	 */
	public static boolean isLocalhost(String ip) {
		if (ip == null) {
			return false;
		}

		return "127.0.0.1".equals(ip) ||
				"0:0:0:0:0:0:0:1".equals(ip) ||
				"::1".equals(ip) ||
				"localhost".equalsIgnoreCase(ip);
	}
}
