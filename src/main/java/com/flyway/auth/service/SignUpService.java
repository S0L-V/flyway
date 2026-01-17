package com.flyway.auth.service;

import com.flyway.auth.domain.KakaoUserInfo;
import com.flyway.auth.dto.EmailSignUpRequest;
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
     * OAuth(KAKAO) 신규 가입 처리
     * - users/user_identity/user_profile 생성 후 반환
     */
    User signUpKakaoUser(KakaoUserInfo userInfo);
}
