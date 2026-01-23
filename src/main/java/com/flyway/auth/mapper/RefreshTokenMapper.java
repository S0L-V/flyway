package com.flyway.auth.mapper;

import com.flyway.auth.domain.EmailVerificationToken;
import com.flyway.auth.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {

    /**
     * Refresh Token 저장
     */
    int insert(@Param("token") RefreshToken token);

    /**
     * token_hash로 Refresh Token 조회 (쿠키 원문 -> hash 후 조회)
     */
    RefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Refresh Token 회전 처리 (정상 재발급 시 기존 토큰을 rotated 처리)
     */
    int markRotated(@Param("refreshTokenId") String refreshTokenId,
                    @Param("now") LocalDateTime now,
                    @Param("replacedByTokenId") String replacedByTokenId);

    /**
     * 단일 Refresh Token 폐기 (로그아웃: 현재 세션만 종료)
     */
    int revokeById(@Param("refreshTokenId") String refreshTokenId,
                   @Param("now") LocalDateTime now);

    /**
     * 유저의 모든 Refresh Token 폐기
     */
    int revokeAllByUserId(@Param("userId") String userId,
                          @Param("now") LocalDateTime now);

    /**
     * 만료 + revoke된 토큰 정리
     */
    int deleteExpiredRevokedTokens(@Param("now") LocalDateTime now);
}
