package com.flyway.util;

import javax.servlet.http.HttpServletRequest;

/**
 * IP 주소 추출 유틸리티
 */
public class IpUtil {

	/**
	 * 실제 클라이언트 IP 주소 가져오기
	 * 프록시, 로드밸런서, CDN을 고려한 IP 추출
	 */
	public static String getClientIp(HttpServletRequest req) {
		String ip = null;

		// 1. X-Forwarded-For 헤더 확인 (가장 일반적)
		ip = req.getHeader("X-Forwarded-For");
		if (isValidIp(ip)) {
			// 여러 IP가 있을 경우 첫 번째가 실제 클라이언트 IP
			return ip.split(",")[0].trim();
		}

		// 2. Proxy-Client-IP 헤더
		ip = req.getHeader("Proxy-Client-IP");
		if (isValidIp(ip)) {
			return ip;
		}

		// 3. WL-Proxy-Client-IP 헤더 (WebLogic)
		ip = req.getHeader("WL-Proxy-Client-IP");
		if (isValidIp(ip)) {
			return ip;
		}

		// 4. HTTP_CLIENT_IP 헤더
		ip = req.getHeader("HTTP_CLIENT_IP");
		if (isValidIp(ip)) {
			return ip;
		}

		// 5. HTTP_X_FORWARDED_FOR 헤더
		ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		if (isValidIp(ip)) {
			return ip;
		}

		// 6. X-Real-IP 헤더 (nginx)
		ip = req.getHeader("X-Real-IP");
		if (isValidIp(ip)) {
			return ip;
		}

		// 7. X-RealIP 헤더
		ip = req.getHeader("X-Real-IP");
		if (isValidIp(ip)) {
			return ip;
		}

		// 8. REMOTE_ADDR (직접 연결)
		ip = req.getRemoteAddr();
		return ip != null ? ip : "UNKNOWN";
	}

	/**
	 * IP 주소 유효성 검증
	 */
	private static boolean isValidIp(String ip) {
		return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
	}
}
