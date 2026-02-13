package com.flyway.security.filter;

import com.flyway.auth.controller.AuthController;
import com.flyway.auth.service.AuthTokenService;
import com.flyway.auth.service.KakaoLoginService;
import com.flyway.auth.service.SignUpService;
import com.flyway.security.service.EmailUserDetailsService;
import com.flyway.security.service.UserIdUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CsrfProtectionTest {

    private MockMvc mockMvc;
    private AuthTokenService authTokenService;
    private CookieCsrfTokenRepository csrfTokenRepository;

    @BeforeEach
    void setUp() {
        SignUpService signUpService = mock(SignUpService.class);
        KakaoLoginService kakaoLoginService = mock(KakaoLoginService.class);
        EmailUserDetailsService emailUserDetailsService = mock(EmailUserDetailsService.class);
        UserIdUserDetailsService userIdUserDetailsService = mock(UserIdUserDetailsService.class);
        authTokenService = mock(AuthTokenService.class);

        AuthController controller = new AuthController(
                signUpService,
                kakaoLoginService,
                emailUserDetailsService,
                userIdUserDetailsService,
                authTokenService
        );

        csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfFilter csrfFilter = new CsrfFilter(csrfTokenRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(csrfFilter)
                .build();
    }

    @Test
    @DisplayName("CSRF 토큰 없이 POST /auth/refresh 요청하면 403을 반환한다")
    void refresh_withoutCsrfToken_returnsForbidden() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효한 CSRF 쿠키+헤더로 POST /auth/refresh 요청하면 통과한다")
    void refresh_withValidCsrfToken_passes() throws Exception {
        doNothing().when(authTokenService).refresh(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        String token = newTokenValue();

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("XSRF-TOKEN", token))
                        .header("X-XSRF-TOKEN", token))
                .andExpect(status().isNoContent());

        verify(authTokenService).refresh(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private String newTokenValue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/csrf");
        CsrfToken token = csrfTokenRepository.generateToken(request);
        return token.getToken();
    }
}
