package com.flyway.user.repository;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.user.domain.UserIdentity;
import org.apache.ibatis.annotations.Param;

public interface UserIdentityRepository {

    /**
     * 인증 정보 생성
     */
    void save(UserIdentity identity);

    /**
     * (인증 제공자, 제공자 유저 ID)로 조회
     * (OAuth 재로그인 시 기존 사용자 식별)
     */
    UserIdentity findByProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    /**
     * provider가 EMAIL인 사용자 중 이메일 중복 여부 확인
     */
    boolean existsEmailIdentity(String email);

    /**
     * EMAIL 가입자의 provider_user_id(이메일) 익명화
     */
    int anonymizeEmailProviderUserIdIfWithdrawn(String userId, String anonymizedEmail);
}
