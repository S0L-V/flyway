package com.flyway.auth.controller;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.service.KakaoLoginService;
import com.flyway.auth.service.SignUpService;
import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.service.EmailUserDetailsService;
import com.flyway.security.service.UserIdUserDetailsService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AuthControllerTest {

    private MockMvc mockMvc;
    private SignUpService signUpService;
    private KakaoLoginService kakaoLoginService;
    private EmailUserDetailsService emailUserDetailsService;
    private UserIdUserDetailsService userIdUserDetailsService;
    private LoginSuccessHandler loginSuccessHandler;

    @BeforeEach
    void setUp() {
        signUpService = Mockito.mock(SignUpService.class);
        kakaoLoginService = Mockito.mock(KakaoLoginService.class);
        emailUserDetailsService = Mockito.mock(EmailUserDetailsService.class);
        userIdUserDetailsService = Mockito.mock(UserIdUserDetailsService.class);
        loginSuccessHandler = Mockito.mock(LoginSuccessHandler.class);

        AuthController controller = new AuthController(
                signUpService,
                kakaoLoginService,
                emailUserDetailsService,
                userIdUserDetailsService,
                loginSuccessHandler
        );

        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(vr)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 시 홈으로 리다이렉트된다")
    void signUp_success_redirectsHome() throws Exception {
        UserDetails userDetails = new User(
                "test@example.com",
                "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        doNothing().when(signUpService).signUp(any(EmailSignUpRequest.class));
        when(emailUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        mockMvc.perform(post("/auth/signup")
                        .param("name", "Tester")
                        .param("email", "test@example.com")
                        .param("rawPassword", "password")
                        .param("attemptId", "attempt-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(signUpService).signUp(any(EmailSignUpRequest.class));
        verify(loginSuccessHandler).issueAccessTokenCookie(any(), eq("test@example.com"));
    }

    @Test
    @DisplayName("회원가입 실패 시 signup 뷰로 돌아간다")
    void signUp_invalidAttempt_showsError() throws Exception {
        doThrow(new BusinessException(ErrorCode.USER_INVALID_SIGN_UP_ATTEMPT))
                .when(signUpService)
                .signUp(any(EmailSignUpRequest.class));

        mockMvc.perform(post("/auth/signup")
                        .param("name", "Tester")
                        .param("email", "test@example.com")
                        .param("rawPassword", "password")
                        .param("attemptId", "attempt-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", ErrorCode.USER_INVALID_SIGN_UP_ATTEMPT.getMessage()));
    }
}
