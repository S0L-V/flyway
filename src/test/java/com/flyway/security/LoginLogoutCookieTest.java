package com.flyway.security;

import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.jwt.JwtProperties;
import com.flyway.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginLogoutCookieTest {

    @Test
    @DisplayName("로그인 시 accessToken 쿠키가 내려가고 세션이 생성된다")
    void login_adds_accessToken_cookie_and_session_created() throws Exception {
        JwtProvider jwtProvider = mock(JwtProvider.class);
        JwtProperties jwtProperties = mock(JwtProperties.class);
        LoginSuccessHandler handler = new LoginSuccessHandler(jwtProvider, jwtProperties);

        when(jwtProvider.createAccessToken("user-123")).thenReturn("access.jwt.token");
        when(jwtProperties.getAccessTokenTtlSeconds()).thenReturn(3600L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                User.withUsername("user-123").password("pw").authorities("ROLE_USER").build(),
                null
        );

        handler.onAuthenticationSuccess(request, response, authentication);

        assertTrue(hasCookie(response, "accessToken"));
        assertNotNull(session);
    }

    @Test
    @DisplayName("로그아웃 시 accessToken/JSESSIONID 삭제 쿠키가 내려가고 세션이 무효화된다")
    void logout_clears_cookies_and_invalidates_session() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        SecurityContextLogoutHandler contextLogoutHandler = new SecurityContextLogoutHandler();
        CookieClearingLogoutHandler cookieClearingLogoutHandler =
                new CookieClearingLogoutHandler("accessToken", "JSESSIONID");

        contextLogoutHandler.logout(request, response, null);
        cookieClearingLogoutHandler.logout(request, response, null);

        assertTrue(session.isInvalid());
        assertTrue(hasClearedCookie(response, "accessToken"));
        assertTrue(hasClearedCookie(response, "JSESSIONID"));
    }

    private boolean hasCookie(MockHttpServletResponse response, String name) {
        Cookie cookie = response.getCookie(name);
        if (cookie != null) return true;
        List<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        return headers.stream().anyMatch(h -> h.startsWith(name + "="));
    }

    private boolean hasClearedCookie(MockHttpServletResponse response, String name) {
        Cookie cookie = response.getCookie(name);
        if (cookie != null) return cookie.getMaxAge() == 0;
        List<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        return headers.stream().anyMatch(h -> h.startsWith(name + "=") && h.contains("Max-Age=0"));
    }
}
