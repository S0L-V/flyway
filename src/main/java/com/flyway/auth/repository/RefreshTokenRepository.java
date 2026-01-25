package com.flyway.auth.repository;

import com.flyway.auth.domain.RefreshToken;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface RefreshTokenRepository {

    int insert(RefreshToken token);

    RefreshToken findByTokenHash(String tokenHash);

    int markRotated(String refreshTokenId, LocalDateTime now, String replacedByTokenId);

    int revokeById(String refreshTokenId, LocalDateTime now);

    int revokeAllByUserId(String userId, LocalDateTime now);

    int deleteExpiredRevokedTokens(LocalDateTime now);

    int revokeAllActiveByUserId(String userId, LocalDateTime now);
}
