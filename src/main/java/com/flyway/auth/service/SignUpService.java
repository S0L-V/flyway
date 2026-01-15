package com.flyway.auth.service;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.dto.KakaoLoginRequest;
import com.flyway.user.domain.User;

public interface SignUpService {

    /**
     * 회원가입
     * - users 생성
     * - user_identity 생성 (provider=EMAIL, providerUserId=email)
     * - user_profile 기본 row (user_id, name) 생성
     */
    void signUp(EmailSignUpRequest request);

    /**
     * OAuth(KAKAO) 로그인/가입 처리
     * - (provider, providerUserId)로 user_identity 조회
     * - 있으면 기존 user 반환(로그인 처리)
     * - 없으면 users/user_identity/user_profile 생성 후 반환
     */
    User handleKakaoLogin(KakaoLoginRequest request);
}
