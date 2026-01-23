package com.flyway.auth.repository;

import com.flyway.auth.domain.SignUpAttempt;
import com.flyway.auth.domain.SignUpStatus;

import java.time.LocalDateTime;

public interface SignUpAttemptRepository {

    void insert(SignUpAttempt attempt);

    int markVerifiedIfPending(String attemptId, LocalDateTime verifiedAt);

    int consumeIfVerified(String attemptId, String email, LocalDateTime consumedAt);

    SignUpStatus findStatusById(String attemptId);

    int expireIfPendingAndExpired(String attemptId, LocalDateTime now);
}
