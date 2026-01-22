package com.flyway.auth.repository;

import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.mapper.EmailVerificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationMapper emailVerificationMapper;

    @Override
    public void insertEmailVerificationToken(EmailVerificationToken token) {
        emailVerificationMapper.insertEmailVerificationToken(token);
    }

    @Override
    public EmailVerificationToken findByTokenHash(String tokenHash) {
        return emailVerificationMapper.findByTokenHash(tokenHash);
    }

    @Override
    public int markTokenUsed(String emailVerificationTokenId, LocalDateTime usedAt) {
        return emailVerificationMapper.markTokenUsed(emailVerificationTokenId, usedAt);
    }

    @Override
    public int countVerifiedByEmailPurpose(String email, String purpose, LocalDateTime now) {
        return emailVerificationMapper.countVerifiedByEmailPurpose(email, purpose, now);
    }
}
