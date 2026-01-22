package com.flyway.auth.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpAttempt {
    private String attemptId;
    private String email;
    private SignUpStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime consumedAt;


    @Builder
    private SignUpAttempt(
            String attemptId,
            String email,
            SignUpStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this.attemptId = attemptId;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void verify() {
        this.status = SignUpStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void consume() {
        this.status = SignUpStatus.CONSUMED;
        this.consumedAt = LocalDateTime.now();
    }
}
