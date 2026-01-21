package com.flyway.auth.repository;

import com.flyway.auth.domain.EmailVerificationToken;

import java.time.LocalDateTime;

public interface EmailVerificationRepository {

    void insertEmailVerificationToken(EmailVerificationToken token);

    EmailVerificationToken findByTokenHash(String tokenHash);

    int markTokenUsed(String emailVerificationTokenId, LocalDateTime usedAt);

    int countVerifiedByEmailPurpose(String email, String purpose, LocalDateTime now);
}
