package com.flyway.user.mapper;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.user.domain.UserIdentity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserIdentityMapper {

    /**
     * 인증 정보 생성 (EMAIL / KAKAO)
     */
    void insertIdentity(UserIdentity identity);

    /**
     * Email 가입 회원 중 중복 이메일 보유 회원 조회
     */
    boolean existsEmailIdentity(@Param("email") String email);

    /**
     * userId로 인증 정보 조회
     */
    UserIdentity findByUserId(String email);

    /**
     * provider + providerUserId로 인증 정보 조회 (OAuth 재로그인 시 기존 사용자 찾기)
     */
    UserIdentity findByProviderUserId(
            @Param("provider") AuthProvider provider,
            @Param("providerUserId") String providerUserId
    );

    /**
     * provider_user_id 익명화
     */
    int anonymizeProviderUserIdIfWithdrawn(@Param("userId") String userId,
                                           @Param("provider") String provider,
                                           @Param("anonymizedProviderUserId") String anonymizedProviderUserId);
}
