package com.flyway.security;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.security.handler.LoginSuccessHandler;
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
import static org.mockito.Mockito.*;

class LoginLogoutCookieTest {

    @Test
    @DisplayName("로그인 성공 시 토큰 쿠키 발급을 AuthTokenService에 위임한다q")
    void login_delegates_to_authTokenService() throws Exception {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        LoginSuccessHandler handler = new LoginSuccessHandler(authTokenService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                User.withUsername("user-123").password("pw").authorities("ROLE_USER").build(),
                null
        );

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(authTokenService).issueLoginCookies(request, response, "user-123");
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
