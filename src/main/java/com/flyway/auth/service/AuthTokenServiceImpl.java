package com.flyway.auth.service;

import com.flyway.auth.domain.RefreshToken;
import com.flyway.auth.repository.RefreshTokenRepository;
import com.flyway.auth.util.TokenHasher;
import com.flyway.security.jwt.JwtProperties;
import com.flyway.security.jwt.JwtProvider;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final String ACCESS_COOKIE = "accessToken";
    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String ACCESS_COOKIE_PATH = "/";
    private static final String REFRESH_COOKIE_PATH = "/auth";

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;

    @Transactional
    public void issueLoginCookies(HttpServletRequest request, HttpServletResponse response, String userId) {
        LocalDateTime now = LocalDateTime.now();

        /* accessToken 추가 */
        addAccessTokenCookie(response, userId);

        /* refreshToken 추가 (DB 저장 + 쿠키) */
        String refreshRaw = generateRefreshTokenRaw();
        String refreshId = UUID.randomUUID().toString();
        String hash = tokenHasher.hash(refreshRaw);

        long refreshTtlSeconds = jwtProperties.getRefreshTokenTtlSeconds();
        LocalDateTime expiresAt = now.plusSeconds(refreshTtlSeconds);

        RefreshToken token = RefreshToken.builder()
                .refreshTokenId(refreshId)
                .userId(userId)
                .tokenHash(hash)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.insert(token);
        addRefreshTokenCookie(response, refreshRaw, refreshTtlSeconds);
    }

    /* 재발급: refresh 검증 + 회전 + 새 access/refresh 발급 */
    @Transactional
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        LocalDateTime now = LocalDateTime.now();

        String refreshRaw = readCookie(request, REFRESH_COOKIE);
        if (!StringUtils.hasText(refreshRaw)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_MISSING);
        }

        String hash = tokenHasher.hash(refreshRaw);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash);
        if (stored == null) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        /* 만료/폐기 체크 */
        if (stored.getRevokedAt() != null || !stored.getExpiresAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        /* 재사용 탐지 */
        if (stored.getRotatedAt() != null) {
            refreshTokenRepository.revokeAllByUserId(stored.getUserId(), now);
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_REUSED);
        }

        /* refreshToken 재발급 */
        String newRefreshRaw = generateRefreshTokenRaw();
        String newRefreshId = UUID.randomUUID().toString();
        String newHash = tokenHasher.hash(newRefreshRaw);

        long refreshTtlSeconds = jwtProperties.getRefreshTokenTtlSeconds();
        LocalDateTime newExpiresAt = now.plusSeconds(refreshTtlSeconds);

        RefreshToken newToken = RefreshToken.builder()
                .refreshTokenId(newRefreshId)
                .userId(stored.getUserId())
                .tokenHash(newHash)
                .issuedAt(now)
                .expiresAt(newExpiresAt)
                .build();

        /* 새 refreshToken 저장 */
        refreshTokenRepository.insert(newToken);

        /* 기존 refresh 회전 처리 (1건만 성공) */
        int rotated = refreshTokenRepository.markRotated(
                stored.getRefreshTokenId(), now, newRefreshId
        );

        /* 동시 요청/레이스: 이미 회전됐거나 revoke인 경우 */
        if (rotated == 0) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_ALREADY_USED);
        }

        /* AccessToken 재발급 + 쿠키 세팅 */
        addAccessTokenCookie(response, stored.getUserId());
        addRefreshTokenCookie(response, newRefreshRaw, refreshTtlSeconds);
        log.debug("[AUTH] refresh token issued. userId={}", stored.getUserId());
    }

    /* 로그아웃: refresh 폐기 + 쿠키 삭제 */
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        LocalDateTime now = LocalDateTime.now();

        String refreshRaw = readCookie(request, REFRESH_COOKIE);
        if (StringUtils.hasText(refreshRaw)) {
            String hash = tokenHasher.hash(refreshRaw);
            RefreshToken stored = refreshTokenRepository.findByTokenHash(hash);
            if (stored != null && stored.getRevokedAt() == null) {
                refreshTokenRepository.revokeById(stored.getRefreshTokenId(), now);
            }
        }

        deleteCookie(response, ACCESS_COOKIE, ACCESS_COOKIE_PATH);
        deleteCookie(response, REFRESH_COOKIE, REFRESH_COOKIE_PATH);
    }

    @Transactional
    public void revokeAllRefreshTokens(String userId, LocalDateTime now) {
        refreshTokenRepository.revokeAllByUserId(userId, now);
    }


    /* 쿠키 Helpers */
    private void addAccessTokenCookie(HttpServletResponse response, String userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        long ttl = jwtProperties.getAccessTokenTtlSeconds();

        ResponseCookie cookie = ResponseCookie.from(ACCESS_COOKIE, accessToken)
                .httpOnly(true)
                .secure(false) // 배포(HTTPS)에서 true
                .sameSite("Lax")
                .path(ACCESS_COOKIE_PATH)
                .maxAge(ttl)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshRaw, long ttl) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshRaw)
                .httpOnly(true)
                .secure(false) // https면 true
                .sameSite("Lax")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(ttl)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String generateRefreshTokenRaw() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
