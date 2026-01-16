package com.flyway.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties props;

    /**
     * 테스트 및 시간 제어를 위해 Clock을 분리하여 사용
     */
    private final Clock clock = Clock.systemUTC();

    private SecretKey key;

    /**
     * 애플리케이션 시작 시 JWT 서명용 SecretKey를 초기화
     */
    @PostConstruct
    void init() {
        String secret = props.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자 식별자(userId)를 기반으로 Access Token을 생성
     */
    public String createAccessToken(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(props.getAccessTokenTtlSeconds());

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * JWT를 파싱하고 서명 및 만료 여부를 검증
     */
    public Jws<Claims> parse(String token) {
        String normalized = normalize(token);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(normalized);
    }

    /**
     * JWT에서 subject(userId)를 추출하고 유효하지 않으면 예외를 발생
     */
    public String getSubjectOrThrow(String token) {
        try {
            return parse(token).getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }
    }

    /**
     * 토큰 정규화
     */
    private String normalize(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token is null");
        }
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            t = t.substring(7).trim();
        }
        if (t.isEmpty()) {
            throw new IllegalArgumentException("token is blank");
        }
        return t;
    }
}
