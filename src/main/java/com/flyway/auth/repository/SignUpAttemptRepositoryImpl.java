package com.flyway.auth.repository;

import com.flyway.auth.domain.SignUpAttempt;
import com.flyway.auth.domain.SignUpStatus;
import com.flyway.auth.mapper.SignUpAttemptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class SignUpAttemptRepositoryImpl implements SignUpAttemptRepository {

    private final SignUpAttemptMapper signUpAttemptMapper;

    @Override
    public void insert(SignUpAttempt attempt) {
        signUpAttemptMapper.insert(attempt);
    }

    @Override
    public int expirePendingAttemptsByEmail(String email, LocalDateTime now) {
        return signUpAttemptMapper.expirePendingAttemptsByEmail(email, now);
    }

    @Override
    public int markVerifiedIfPending(String attemptId, LocalDateTime verifiedAt) {
        return signUpAttemptMapper.markVerifiedIfPending(attemptId, verifiedAt);
    }

    @Override
    public int consumeIfVerified(String attemptId, String email, LocalDateTime consumedAt) {
        return signUpAttemptMapper.consumeIfVerified(attemptId, email, consumedAt);
    }

    @Override
    public SignUpStatus findStatusById(String attemptId) {
        String status = signUpAttemptMapper.findStatusById(attemptId);
        return status == null ? null : SignUpStatus.valueOf(status);
    }

    @Override
    public int expireIfPendingAndExpired(String attemptId, LocalDateTime now) {
        return signUpAttemptMapper.expireIfPendingAndExpired(attemptId, now);
    }
}
