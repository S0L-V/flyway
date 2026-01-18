package com.flyway.security.handler;

import com.flyway.security.jwt.JwtProperties;
import com.flyway.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginSuccessHandlerTest {

    @Test
    @DisplayName("로그인 성공 시 Set-Cookie로 accessToken 쿠키가 내려간다")
    void onAuthenticationSuccess_setsAccessTokenCookie() throws Exception {
        JwtProvider jwtProvider = mock(JwtProvider.class);
        JwtProperties jwtProperties = mock(JwtProperties.class);
        LoginSuccessHandler handler = new LoginSuccessHandler(jwtProvider, jwtProperties);

        Authentication authentication = mock(Authentication.class);
        UserDetails principal = User.withUsername("user-123")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(authentication.getPrincipal()).thenReturn(principal);

        when(jwtProvider.createAccessToken("user-123")).thenReturn("access.jwt.token");
        when(jwtProperties.getAccessTokenTtlSeconds()).thenReturn(3600L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("accessToken=access.jwt.token"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertEquals("/", response.getRedirectedUrl());

        verify(jwtProvider).createAccessToken("user-123");
    }
}
