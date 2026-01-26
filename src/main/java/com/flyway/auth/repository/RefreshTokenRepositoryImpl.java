package com.flyway.auth.repository;

import com.flyway.auth.domain.RefreshToken;
import com.flyway.auth.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public int insert(RefreshToken token) {
        return refreshTokenMapper.insert(token);
    }

    @Override
    public RefreshToken findByTokenHash(String tokenHash) {
        return refreshTokenMapper.findByTokenHash(tokenHash);
    }

    @Override
    public int markRotated(String refreshTokenId, LocalDateTime now, String replacedByTokenId) {
        return refreshTokenMapper.markRotated(refreshTokenId, now, replacedByTokenId);
    }

    @Override
    public int revokeById(String refreshTokenId, LocalDateTime now) {
        return refreshTokenMapper.revokeById(refreshTokenId, now);
    }

    @Override
    public int revokeAllByUserId(String userId, LocalDateTime now) {
        return refreshTokenMapper.revokeAllByUserId(userId, now);
    }

    @Override
    public int deleteExpiredRevokedTokens(LocalDateTime now) {
        return refreshTokenMapper.deleteExpiredRevokedTokens(now);
    }
}
