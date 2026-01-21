package com.flyway.auth.service;

public interface EmailVerificationService {

    /**
     * 회원가입 목적 이메일 인증 토큰 발급 및 메일 전송
     */
    void issueSignupVerification(String email);

    /**
     * 회원가입 이메일 인증 링크 검증
     */
    String verifySignupToken(String token);

    /**
     * 회원가입 이메일 인증 완료 여부 확인
     */
    boolean isSignupVerified(String email);
}
