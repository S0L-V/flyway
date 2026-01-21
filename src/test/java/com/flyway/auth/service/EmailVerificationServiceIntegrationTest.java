package com.flyway.auth.service;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.repository.EmailVerificationRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.template.common.mail.MailSender;
import com.flyway.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceIntegrationTest {

    private EmailVerificationRepository emailVerificationRepository;
    private TokenHasher tokenHasher;
    private EmailVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        emailVerificationRepository = Mockito.mock(EmailVerificationRepository.class);
        MailSender mailSender = Mockito.mock(MailSender.class);
        tokenHasher = Mockito.mock(TokenHasher.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        service = new EmailVerificationServiceImpl(
                emailVerificationRepository,
                mailSender,
                tokenHasher,
                userMapper
        );
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "ttlMinutes", 15L);
    }

    @Test
    @DisplayName("만료된 토큰은 인증에 실패하고, 인증 상태는 false")
    void verifySignupToken_expiredToken_fails() {
        String email = "expired@example.com";
        String token = "expired-token";
        String tokenHash = "hash-expired";
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken record = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email(email)
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash(tokenHash)
                .expiresAt(now.minusMinutes(1))
                .createdAt(now.minusMinutes(2))
                .build();

        when(tokenHasher.hash(token)).thenReturn(tokenHash);
        when(emailVerificationRepository.findByTokenHash(tokenHash)).thenReturn(record);
        when(emailVerificationRepository.countVerifiedByEmailPurpose(
                eq(email),
                eq(EmailVerificationPurpose.SIGNUP.name()),
                any(LocalDateTime.class)
        )).thenReturn(0);

        assertThatThrownBy(() -> service.verifySignupToken(token))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(service.isSignupVerified(email)).isFalse();
        verify(emailVerificationRepository, never())
                .markTokenUsed(anyString(), any(LocalDateTime.class));
    }

}
