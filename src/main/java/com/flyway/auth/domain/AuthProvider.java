package com.flyway.auth.domain;

import lombok.Getter;

@Getter
public enum AuthProvider {
    EMAIL("이메일", "이메일과 비밀번호를 이용한 로컬 로그인"),
    KAKAO("카카오", "카카오 OAuth를 통한 소셜 로그인");

    private final String displayName;
    private final String description;

    AuthProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
