package com.flyway.auth.controller;

import com.flyway.auth.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class EmailVerificationViewControllerTest {

    private MockMvc mockMvc;
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = Mockito.mock(EmailVerificationService.class);
        EmailVerificationViewController controller =
                new EmailVerificationViewController(emailVerificationService);

        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(vr)
                .build();
    }

    @Test
    @DisplayName("인증 링크 검증 성공 - 성공 메시지 출력")
    void verifySignupToken_success() throws Exception {
        Mockito.when(emailVerificationService.verifySignupToken(anyString()))
                .thenReturn("test@example.com");

        mockMvc.perform(get("/auth/email/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("email-verify"))
                .andExpect(model().attribute("success", true))
                .andExpect(model().attribute("title", "이메일 인증 완료"))
                .andExpect(model().attribute("statusMessage", "인증이 완료되었습니다."))
                .andExpect(model().attribute("hintMessage", "회원가입 페이지로 돌아가 인증 확인을 눌러 주세요."));
    }

    @Test
    @DisplayName("인증 링크 검증 실패 - 실패 메시지 출력")
    void verifySignupToken_fail() throws Exception {
        doThrow(new IllegalArgumentException("유효하지 않은 인증 링크입니다."))
                .when(emailVerificationService)
                .verifySignupToken(anyString());

        mockMvc.perform(get("/auth/email/verify")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("email-verify"))
                .andExpect(model().attribute("success", false))
                .andExpect(model().attribute("title", "이메일 인증 실패"))
                .andExpect(model().attribute("statusMessage", "유효하지 않은 인증 링크입니다."))
                .andExpect(model().attribute("hintMessage", "인증메일을 다시 요청해 주세요."));
    }
}
