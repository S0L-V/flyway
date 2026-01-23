package com.flyway.auth.integration;

import com.flyway.auth.domain.EmailVerificationPurpose;
import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.mapper.EmailVerificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MyBatisTestConfig.class)
@Transactional
class EmailVerificationMapperIT {

    @Autowired
    private EmailVerificationMapper mapper;

    @Test
    @DisplayName("토큰 저장 후 해시로 조회된다")
    void insertAndFind() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .emailVerificationTokenId(UUID.randomUUID().toString())
                .email("test@example.com")
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash("hash-1")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .attemptId("attempt-1")
                .build();

        mapper.insertEmailVerificationToken(token);

        EmailVerificationToken found = mapper.findByTokenHash("hash-1");
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getAttemptId()).isEqualTo("attempt-1");
    }

    @Test
    @DisplayName("토큰 used 업데이트는 한 번만 성공한다")
    void markTokenUsed_once() {
        String tokenId = UUID.randomUUID().toString();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .emailVerificationTokenId(tokenId)
                .email("test@example.com")
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash("hash-2")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .attemptId("attempt-2")
                .build();
        mapper.insertEmailVerificationToken(token);

        int updated = mapper.markTokenUsed(tokenId, LocalDateTime.now());
        int updatedAgain = mapper.markTokenUsed(tokenId, LocalDateTime.now().plusMinutes(1));

        assertThat(updated).isEqualTo(1);
        assertThat(updatedAgain).isEqualTo(0);
    }

    @Test
    @DisplayName("attempt + email + purpose 기준으로 verified 여부를 조회한다")
    void existsUsedTokenByEmailAttempt() {
        String tokenId = UUID.randomUUID().toString();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .emailVerificationTokenId(tokenId)
                .email("test@example.com")
                .purpose(EmailVerificationPurpose.SIGNUP)
                .tokenHash("hash-3")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .attemptId("attempt-3")
                .build();
        mapper.insertEmailVerificationToken(token);

        mapper.markTokenUsed(tokenId, LocalDateTime.now());

        int count = mapper.existsUsedTokenByEmailAttempt(
                "test@example.com",
                "attempt-3",
                EmailVerificationPurpose.SIGNUP.name(),
                LocalDateTime.now()
        );
        assertThat(count).isEqualTo(1);
    }
}
