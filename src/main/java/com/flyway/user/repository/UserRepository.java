package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.domain.User;

public interface UserRepository {

    /**
     * 사용자 ID로 조회
     */
    User findById(String userId);

    /**
     * 이메일 로그인 회원 중 email로 회원 조회
     */
    User findByEmailForLogin(String email);

    /**
     * 사용자 생성
     */
    void save(User user);

    /**
     * 사용자 이메일 업데이트 (OAuth 온보딩 완료 시)
     */
    void updateEmail(String userId, String email);

    /**
     * 사용자 상태 변경
     */
    void updateStatus(String userId, AuthStatus status);
}
