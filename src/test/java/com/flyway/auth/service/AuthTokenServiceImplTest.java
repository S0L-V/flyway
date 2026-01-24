package com.flyway.auth.service;

import com.flyway.auth.domain.RefreshToken;
import com.flyway.auth.repository.RefreshTokenRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.security.jwt.JwtProperties;
import com.flyway.security.jwt.JwtProvider;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenServiceImplTest {

    private JwtProvider jwtProvider;
    private JwtProperties jwtProperties;
    private RefreshTokenRepository refreshTokenRepository;
    private TokenHasher tokenHasher;
    private AuthTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        jwtProvider = Mockito.mock(JwtProvider.class);
        jwtProperties = Mockito.mock(JwtProperties.class);
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        tokenHasher = Mockito.mock(TokenHasher.class);

        service = new AuthTokenServiceImpl(
                jwtProvider,
                jwtProperties,
                refreshTokenRepository,
                tokenHasher
        );
    }

    @Test
    @DisplayName("accessToken 만료 시 refresh 요청으로 access/refresh 쿠키가 재발급된다")
    void refresh_reissuesTokens_whenRefreshValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "raw-refresh"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenHasher.hash("raw-refresh")).thenReturn("hash-refresh");
        when(jwtProvider.createAccessToken("user-1")).thenReturn("new-access");
        when(jwtProperties.getRefreshTokenTtlSeconds()).thenReturn(3600L);
        when(jwtProperties.getAccessTokenTtlSeconds()).thenReturn(600L);

        RefreshToken stored = RefreshToken.builder()
                .refreshTokenId("refresh-id")
                .userId("user-1")
                .tokenHash("hash-refresh")
                .issuedAt(LocalDateTime.now().minusMinutes(1))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(refreshTokenRepository.findByTokenHash("hash-refresh")).thenReturn(stored);
        when(refreshTokenRepository.markRotated(eq("refresh-id"), any(LocalDateTime.class), anyString()))
                .thenReturn(1);

        service.refresh(request, response);

        verify(refreshTokenRepository).insert(any(RefreshToken.class));
        verify(refreshTokenRepository).markRotated(eq("refresh-id"), any(LocalDateTime.class), anyString());

        List<String> cookies = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(value -> value.contains("accessToken="));
        assertThat(cookies).anyMatch(value -> value.contains("refreshToken="));
    }

    @Test
    @DisplayName("refreshToken 쿠키가 없으면 예외가 발생한다")
    void refresh_missingCookie_throws() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> service.refresh(request, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.AUTH_REFRESH_TOKEN_MISSING.getMessage());
    }
}
