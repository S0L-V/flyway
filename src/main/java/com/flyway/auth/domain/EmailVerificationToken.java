package com.flyway.auth.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    private String emailVerificationTokenId;
    private String email;
    private EmailVerificationPurpose purpose;

    @ToString.Exclude
    private String tokenHash;

    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    private String attemptId;

    @Builder
    private EmailVerificationToken(
            String emailVerificationTokenId,
            String email,
            EmailVerificationPurpose purpose,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime usedAt,
            LocalDateTime createdAt,
            String attemptId
    ) {
        this.emailVerificationTokenId = emailVerificationTokenId;
        this.email = email;
        this.purpose = purpose;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
        this.attemptId = attemptId;
    }
}
