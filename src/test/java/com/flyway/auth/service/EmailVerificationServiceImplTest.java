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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
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
        ReflectionTestUtils.setField(service, "attemptTtlMinutes", 10L);
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
    @DisplayName("인증 링크 검증 성공 시 토큰과 시도가 처리된다")
    void verifySignupToken_success_marksTokenAndAttempt() {
        String attemptId = "attempt-1";
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
                .thenReturn(EmailVerificationToken.builder()
                        .emailVerificationTokenId("token-id")
                        .email("test@example.com")
                        .purpose(EmailVerificationPurpose.SIGNUP)
                        .tokenHash("token-hash")
                        .attemptId(attemptId)
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .createdAt(LocalDateTime.now())
                        .build());
        when(tokenRepository.markTokenUsed(eq("token-id"), any(LocalDateTime.class))).thenReturn(1);
        when(signUpAttemptRepository.markVerifiedIfPending(eq(attemptId), any(LocalDateTime.class))).thenReturn(1);

        String email = service.verifySignupToken("raw-token", attemptId);

        assertThat(email).isEqualTo("test@example.com");
        verify(tokenRepository).markTokenUsed(eq("token-id"), any(LocalDateTime.class));
        verify(signUpAttemptRepository).markVerifiedIfPending(eq(attemptId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("인증 링크의 attemptId가 다르면 검증 실패")
    void verifySignupToken_attemptMismatch_throws() {
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
                .thenReturn(EmailVerificationToken.builder()
                        .emailVerificationTokenId("token-id")
                        .email("test@example.com")
                        .purpose(EmailVerificationPurpose.SIGNUP)
                        .tokenHash("token-hash")
                        .attemptId("attempt-a")
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .createdAt(LocalDateTime.now())
                        .build());

        assertThatThrownBy(() -> service.verifySignupToken("raw-token", "attempt-b"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void verifySignupToken_expired_throws() {
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
                .thenReturn(EmailVerificationToken.builder()
                        .emailVerificationTokenId("token-id")
                        .email("test@example.com")
                        .purpose(EmailVerificationPurpose.SIGNUP)
                        .tokenHash("token-hash")
                        .attemptId("attempt-1")
                        .expiresAt(LocalDateTime.now().minusMinutes(1))
                        .createdAt(LocalDateTime.now())
                        .build());

        assertThatThrownBy(() -> service.verifySignupToken("raw-token", "attempt-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 사용된 토큰은 검증에 실패한다")
    void verifySignupToken_used_throws() {
        when(tokenHasher.hash(anyString())).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
                .thenReturn(EmailVerificationToken.builder()
                        .emailVerificationTokenId("token-id")
                        .email("test@example.com")
                        .purpose(EmailVerificationPurpose.SIGNUP)
                        .tokenHash("token-hash")
                        .attemptId("attempt-1")
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .usedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build());

        assertThatThrownBy(() -> service.verifySignupToken("raw-token", "attempt-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 발급 실패")
    void issueSignupVerification_invalidEmail_throwsException() {
        assertThatThrownBy(() -> service.issueSignupVerification("bad-email"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(tokenRepository, mailSender);
    }
}
