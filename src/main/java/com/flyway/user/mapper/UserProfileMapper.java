package com.flyway.user.mapper;

import com.flyway.user.domain.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    /**
     * 회원가입 시 이름 추가
     */
    void insertProfile(UserProfile profile);

    /**
     * userId로 프로필 조회
     */
    UserProfile findByUserId(@Param("userId") String userId);


    /**
     * 프로필 업데이트 (동적 SQL, null이 아닌 필드만 업데이트)
     */
    void updateProfile(UserProfile profile);
}
