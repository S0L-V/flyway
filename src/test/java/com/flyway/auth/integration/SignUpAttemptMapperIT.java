package com.flyway.auth.integration;

import com.flyway.auth.domain.SignUpAttempt;
import com.flyway.auth.domain.SignUpStatus;
import com.flyway.auth.mapper.SignUpAttemptMapper;
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
class SignUpAttemptMapperIT {

    @Autowired
    private SignUpAttemptMapper mapper;

    @Test
    @DisplayName("attempt 상태가 VERIFIED -> CONSUMED로 전이된다")
    void verifyAndConsume() {
        SignUpAttempt attempt = SignUpAttempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .email("test@example.com")
                .status(SignUpStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        mapper.insert(attempt);

        int verified = mapper.markVerifiedIfPending(attempt.getAttemptId(), LocalDateTime.now());
        assertThat(verified).isEqualTo(1);
        assertThat(mapper.findStatusById(attempt.getAttemptId())).isEqualTo("VERIFIED");

        int consumed = mapper.consumeIfVerified(attempt.getAttemptId(), "test@example.com", LocalDateTime.now());
        assertThat(consumed).isEqualTo(1);
        assertThat(mapper.findStatusById(attempt.getAttemptId())).isEqualTo("CONSUMED");
    }

    @Test
    @DisplayName("VERIFIED 상태가 아니면 consume이 실패한다")
    void consumeFailsWhenNotVerified() {
        SignUpAttempt attempt = SignUpAttempt.builder()
                .attemptId(UUID.randomUUID().toString())
                .email("test@example.com")
                .status(SignUpStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        mapper.insert(attempt);

        int consumed = mapper.consumeIfVerified(attempt.getAttemptId(), "test@example.com", LocalDateTime.now());
        assertThat(consumed).isEqualTo(0);
        assertThat(mapper.findStatusById(attempt.getAttemptId())).isEqualTo("PENDING");
    }
}
