package com.flyway.auth.controller;

import com.flyway.auth.service.EmailVerificationService;
import com.flyway.template.exception.ErrorCode;
import com.flyway.template.exception.MailSendException;
import com.flyway.template.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailVerificationApiControllerTest {

    private MockMvc mockMvc;
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = Mockito.mock(EmailVerificationService.class);
        EmailVerificationApiController controller =
                new EmailVerificationApiController(emailVerificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("이메일 인증 발급 성공")
    void issueSignupVerification_success() throws Exception {
        when(emailVerificationService.issueSignupVerification(anyString()))
                .thenReturn("attempt-123");

        mockMvc.perform(post("/api/auth/email/issue")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attemptId").value("attempt-123"))
                .andExpect(jsonPath("$.message").value("이메일 인증 메일을 전송했습니다."));
    }

    @Test
    @DisplayName("이메일 인증 발급 실패 - 입력 오류")
    void issueSignupVerification_invalidInput() throws Exception {
        doThrow(new IllegalArgumentException("이메일 형식이 올바르지 않습니다."))
                .when(emailVerificationService)
                .issueSignupVerification(anyString());

        mockMvc.perform(post("/api/auth/email/issue")
                        .param("email", "bad-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("이메일 인증 발급 실패 - 메일 전송 오류")
    void issueSignupVerification_mailFail() throws Exception {
        doThrow(new MailSendException("메일 전송 실패", new RuntimeException("smtp error")))
                .when(emailVerificationService)
                .issueSignupVerification(anyString());

        mockMvc.perform(post("/api/auth/email/issue")
                        .param("email", "test@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("메일 전송에 실패했습니다."));
    }

    @Test
    @DisplayName("이메일 인증 발급 실패 - 중복 이메일")
    void issueSignupVerification_duplicateEmail() throws Exception {
        doThrow(new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS))
                .when(emailVerificationService)
                .issueSignupVerification(anyString());

        mockMvc.perform(post("/api/auth/email/issue")
                        .param("email", "dup@example.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.USER_EMAIL_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_EMAIL_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("이메일 인증 상태 조회 - true")
    void checkSignupVerification_true() throws Exception {
        when(emailVerificationService.isSignupVerified(eq("test@example.com"), anyString()))
                .thenReturn(true);

        mockMvc.perform(get("/api/auth/email/status")
                        .param("email", "test@example.com")
                        .param("attemptId", "attempt-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("이메일 인증 상태 조회 - false")
    void checkSignupVerification_false() throws Exception {
        when(emailVerificationService.isSignupVerified(eq("test@example.com"), anyString()))
                .thenReturn(false);

        mockMvc.perform(get("/api/auth/email/status")
                        .param("email", "test@example.com")
                        .param("attemptId", "attempt-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("이메일 인증 상태 조회 - 입력 오류")
    void checkSignupVerification_invalidInput() throws Exception {
        doThrow(new IllegalArgumentException("이메일 형식이 올바르지 않습니다."))
                .when(emailVerificationService)
                .isSignupVerified(anyString(), anyString());

        mockMvc.perform(get("/api/auth/email/status")
                        .param("email", "bad-email")
                        .param("attemptId", "attempt-789"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
    }
}
