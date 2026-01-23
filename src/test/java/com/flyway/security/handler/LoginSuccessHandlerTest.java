package com.flyway.security.handler;

import com.flyway.auth.service.AuthTokenService;
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
    @DisplayName("로그인 성공 시 AuthTokenService에 쿠키 발급을 위임하고 리다이렉트한다")
    void onAuthenticationSuccess_delegatesToAuthTokenService_andRedirects() throws Exception {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        LoginSuccessHandler handler = new LoginSuccessHandler(authTokenService);

        Authentication authentication = mock(Authentication.class);
        UserDetails principal = User.withUsername("user-123")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(authentication.getPrincipal()).thenReturn(principal);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(authTokenService).issueLoginCookies(request, response, "user-123");
        assertEquals("/", response.getRedirectedUrl());
    }
}
