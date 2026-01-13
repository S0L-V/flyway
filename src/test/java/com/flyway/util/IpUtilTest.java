package com.flyway.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IpUtilTest {

	@Test
	@DisplayName("X-Forwarded-For 헤더에서 첫 번째 IP 추출")
	void testForwardedFor() {
		// given
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("X-Forwarded-For"))
			.thenReturn("123.45.67.89, 10.0.0.1");

		// when & then
		assertThat(IpUtil.getClientIp(req)).isEqualTo("123.45.67.89");
	}

	@Test
	@DisplayName("헤더 없을 떄 RemoteAddr 사용")
	void testRemoteAddr() {
		// given
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("X-Forwarded-For")).thenReturn(null);
		when(req.getRemoteAddr()).thenReturn("211.234.123.45");

		// when & then
		assertThat(IpUtil.getClientIp(req)).isEqualTo("211.234.123.45");
	}

	@Test
	@DisplayName("IPv6 lcoalhost를 IPv4로 변환")
	void testIPv6Localhost() {
		// given
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

		// when & then
		assertThat(IpUtil.getClientIp(req)).isEqualTo("127.0.0.1");
	}

	@Test
	@DisplayName("null request는 UNKNOWN 반환")
	void isNullRequest() {
		assertThat(IpUtil.getClientIp(null)).isEqualTo("UNKNOWN");
	}

	@Test
	@DisplayName("unknown 헤더는 무시하고 RemoteAddr 사용")
	void testUnknownHeader() {
		// given
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getHeader("X-Forwarded-For")).thenReturn("unknown");
		when(req.getRemoteAddr()).thenReturn("192.168.1.100");

		// when & then
		assertThat(IpUtil.getClientIp(req)).isEqualTo("192.168.1.100");
	}
}