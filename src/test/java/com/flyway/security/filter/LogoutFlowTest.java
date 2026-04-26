package com.flyway.security.filter;

import com.flyway.auth.service.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogoutFlowTest {

    private MockMvc mockMvc;
    private AuthTokenService authTokenService;
    private CookieCsrfTokenRepository csrfTokenRepository;

    @BeforeEach
    void setUp() {
        authTokenService = mock(AuthTokenService.class);
        csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        CsrfFilter csrfFilter = new CsrfFilter(csrfTokenRepository);

        SecurityContextLogoutHandler contextLogoutHandler = new SecurityContextLogoutHandler();
        contextLogoutHandler.setInvalidateHttpSession(true);
        contextLogoutHandler.setClearAuthentication(true);

        CookieClearingLogoutHandler cookieClearingLogoutHandler =
                new CookieClearingLogoutHandler("JSESSIONID");

        LogoutFilter logoutFilter = new LogoutFilter("/login",
                (request, response, authentication) -> authTokenService.logout(request, response),
                contextLogoutHandler,
                cookieClearingLogoutHandler
        );
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "POST"));

        mockMvc = MockMvcBuilders.standaloneSetup(new NoopController())
                .addFilters(csrfFilter, logoutFilter)
                .build();
    }

    @Test
    @DisplayName("CSRF 토큰 없이 POST /auth/logout 요청하면 403을 반환한다")
    void logout_withoutCsrfToken_returnsForbidden() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isForbidden());

        verify(authTokenService, never()).logout(any(), any());
    }

    @Test
    @DisplayName("유효한 CSRF 쿠키/헤더로 POST /auth/logout 요청하면 /login으로 리다이렉트되고 로그아웃 핸들러가 호출된다")
    void logout_withValidCsrfToken_redirectsToLogin() throws Exception {
        String token = newTokenValue();
        MockHttpSession session = new MockHttpSession();

        MvcResult result = mockMvc.perform(post("/auth/logout")
                        .session(session)
                        .cookie(new Cookie("XSRF-TOKEN", token))
                        .header("X-XSRF-TOKEN", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        verify(authTokenService).logout(any(), any());
        assertThat(session.isInvalid()).isTrue();

        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(v -> v.startsWith("JSESSIONID=") && v.contains("Max-Age=0"));
    }

    private String newTokenValue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/csrf");
        CsrfToken token = csrfTokenRepository.generateToken(request);
        return token.getToken();
    }

    /* 필터 체인 검증용 플레이스홀더(standaloneSetup은 최소 1개의 컨트롤러 필요) */
    @Controller
    static class NoopController {}
}
