package com.flyway.user.repository;

import com.flyway.user.domain.UserProfile;
import org.apache.ibatis.annotations.Param;

public interface UserProfileRepository {

    /**
     * 사용자 기본 프로필 생성
     */
    void createProfile(UserProfile userProfile);

    /**
     * 사용자 ID로 프로필 조회
     */
    UserProfile findByUserId(String userId);

    /**
     * 사용자 프로필 수정
     * - null이 아닌 필드만 업데이트
     */
    void updateProfile(UserProfile profile);

    /**
     * user_profile 개인정보 NULL 처리
     */
    int nullifyProfileIfWithdrawn(String userId);
}
