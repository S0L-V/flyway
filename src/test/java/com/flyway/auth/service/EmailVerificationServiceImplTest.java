package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationRepository;
import com.flyway.auth.repository.SignUpAttemptRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmailVerificationServiceImplTest {

    private EmailVerificationRepository tokenRepository;
    private SignUpAttemptRepository signUpAttemptRepository;
    private MailSender mailSender;
    private TokenHasher tokenHasher;
    private com.flyway.user.mapper.UserMapper userMapper;
    private EmailVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        tokenRepository = Mockito.mock(EmailVerificationRepository.class);
        signUpAttemptRepository = Mockito.mock(SignUpAttemptRepository.class);
        mailSender = Mockito.mock(MailSender.class);
        tokenHasher = Mockito.mock(TokenHasher.class);
        userMapper = Mockito.mock(com.flyway.user.mapper.UserMapper.class);

        service = new EmailVerificationServiceImpl(
                tokenRepository,
                signUpAttemptRepository,
                mailSender,
                tokenHasher,
                userMapper
        );
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "ttlMinutes", 15L);
    }

    @Test
    @DisplayName("회원가입 이메일 인증 발급 시 토큰 저장 후 메일 발송")
    void issueSignupVerification_savesTokenAndSendsMail() {
        when(tokenHasher.hash(anyString())).thenReturn("hash-value");
        when(userMapper.findByEmailForLogin(anyString())).thenReturn(null);

        service.issueSignupVerification("test@example.com");

        ArgumentCaptor<EmailVerificationToken> captor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).insertEmailVerificationToken(captor.capture());

        EmailVerificationToken saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPurpose()).isEqualTo(EmailVerificationPurpose.SIGNUP);
        assertThat(saved.getTokenHash()).isEqualTo("hash-value");

        verify(mailSender).sendText(
                eq("test@example.com"),
                eq("[Flyway] 이메일 인증 안내"),
                contains("/auth/email/verify?token=")
        );
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 발급 실패")
    void issueSignupVerification_invalidEmail_throwsException() {
        assertThatThrownBy(() -> service.issueSignupVerification("bad-email"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(tokenRepository, mailSender);
    }
}
