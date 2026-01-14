package com.flyway.user.mapper;

import com.flyway.user.domain.UserIdentity;
import com.flyway.auth.domain.AuthProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserIdentityMapper {

    void insertIdentity(UserIdentity identity); // 인증 정보 생성 (EMAIL / KAKAO)

    boolean existsEmailIdentity(@Param("email") String email);

    UserIdentity findByProviderUserId(
            @Param("provider") AuthProvider provider,
            @Param("providerUserId") String providerUserId
    ); // provider + providerUserId로 인증 정보 조회 (OAuth 재로그인 시 기존 사용자 찾기)

    List<UserIdentity> findAllByUserId(@Param("userId") String userId); // userId로 인증 정보 목록 조회
}
