package com.flyway.auth.domain;

import lombok.Getter;

@Getter
public enum AuthStatus {
    ACTIVE("활성화", "정상적으로 서비스 이용이 가능한 상태"),
    BLOCKED("차단", "운영 정책 위반 또는 관리자 조치로 이용이 제한된 상태"),
    ONBOARDING("회원가입 중", "회원가입 절차가 완료되지 않은 상태");

    private final String displayName;
    private final String description;

    AuthStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
