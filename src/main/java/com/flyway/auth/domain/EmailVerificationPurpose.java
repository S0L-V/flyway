package com.flyway.auth.domain;

import lombok.Getter;

@Getter
public enum EmailVerificationPurpose {
    SIGNUP("회원가입", "회원가입을 위한 이메일 인증");

    private final String displayName;
    private final String description;

    EmailVerificationPurpose(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}