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

    boolean existsEmailIdentity(@Param("email") String email);

    /**
     * provider + providerUserId로 인증 정보 조회 (OAuth 재로그인 시 기존 사용자 찾기)
     */
    UserIdentity findByProviderUserId(
            @Param("provider") AuthProvider provider,
            @Param("providerUserId") String providerUserId
    );
}
