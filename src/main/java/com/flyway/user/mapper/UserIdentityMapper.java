package com.flyway.user.mapper;

import com.flyway.user.domain.UserIdentity;
import com.flyway.auth.domain.AuthProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * EMAIL 가입자의 provider_user_id(이메일) 익명화
     */
    int anonymizeEmailProviderUserIdIfWithdrawn(@Param("userId") String userId,
                                                @Param("anonymizedEmail") String anonymizedEmail);
}
