package com.flyway.user.domain;

import com.flyway.auth.domain.AuthStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String email;

    @ToString.Exclude // 민감 정보 제외
    private String passwordHash; // 이메일 로그인용 비밀번호 해시

    @Builder.Default
    private AuthStatus status = AuthStatus.ONBOARDING;

    private LocalDateTime createdAt;
}
