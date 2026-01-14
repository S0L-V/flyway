package com.flyway.user.repository;

import com.flyway.user.domain.UserProfile;

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
}
