package com.flyway.auth.domain;

import lombok.Getter;

@Getter
public enum SignUpStatus {
    PENDING("진행 중", "회원가입 진행 중"),
    VERIFIED("인증 완료", "이메일 인증 완료"),
    CONSUMED("가입 완료", "회원가입 완료"),
    EXPIRED("만료", "가입 유효 시간 만료");

    private final String displayName;
    private final String description;

    SignUpStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}