package com.flyway.auth.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private String refreshTokenId;
    private String userId;
    private String tokenHash;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;
    private LocalDateTime rotatedAt;
    private String replacedByTokenId;
}
