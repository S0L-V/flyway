package com.flyway.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtProviderTest {

    @Test
    @DisplayName("AccessToken 생성 시 subject, issuer, 만료 시간이 정상적으로 설정된다")
    void createAccessToken_success() {
        JwtProperties props = mock(JwtProperties.class);
        when(props.getSecret()).thenReturn("0123456789abcdef0123456789abcdef");
        when(props.getAccessTokenTtlSeconds()).thenReturn(3600L);
        when(props.getIssuer()).thenReturn("flyway");

        JwtProvider provider = new JwtProvider(props);
        provider.init();

        String token = provider.createAccessToken("user-123");
        assertNotNull(token);

        Jws<Claims> parsed = provider.parse(token);
        assertEquals("user-123", parsed.getPayload().getSubject());
        assertEquals("flyway", parsed.getPayload().getIssuer());

        Date issuedAt = parsed.getPayload().getIssuedAt();
        Date expiration = parsed.getPayload().getExpiration();
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("userId가 비어 있으면 AccessToken 생성 시 IllegalArgumentException이 발생한다")
    void createAccessToken_blankUserId_throws() {
        JwtProperties props = mock(JwtProperties.class);
        when(props.getSecret()).thenReturn("0123456789abcdef0123456789abcdef");
        when(props.getAccessTokenTtlSeconds()).thenReturn(3600L);
        when(props.getIssuer()).thenReturn("flyway");

        JwtProvider provider = new JwtProvider(props);
        provider.init();

        assertThrows(IllegalArgumentException.class, () -> provider.createAccessToken(" "));
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰이면 BadCredentialsException이 발생한다")
    void getSubjectOrThrow_invalidToken_throws() {
        JwtProperties props = mock(JwtProperties.class);
        when(props.getSecret()).thenReturn("0123456789abcdef0123456789abcdef");
        when(props.getAccessTokenTtlSeconds()).thenReturn(3600L);
        when(props.getIssuer()).thenReturn("flyway");

        JwtProvider provider = new JwtProvider(props);
        provider.init();

        assertThrows(BadCredentialsException.class, () -> provider.getSubjectOrThrow("not-a-jwt"));
    }
}
