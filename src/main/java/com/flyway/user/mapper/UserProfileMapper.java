package com.flyway.user.mapper;

import com.flyway.user.domain.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    void insertProfile(UserProfile profile); // 회원가입 시 이름 추가

    UserProfile findByUserId(@Param("userId") String userId); // userId로 프로필 조회

    void updateProfile(UserProfile profile); // 프로필 업데이트 (동적 SQL, null이 아닌 필드만 업데이트)
}
