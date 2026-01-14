package com.flyway.user.mapper;

import com.flyway.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 회원 추가
     */
    void insertUser(User user); // 회원 생성

    /**
     * userId로 회원 조회
     */
    User findById(@Param("userId") String userId);

    /**
     * 이메일 업데이트 (OAuth 온보딩 완료 시)
     */
    void updateEmail(
            @Param("userId") String userId,
            @Param("email") String email
    );

    /**
     * 비밀번호 해시 업데이트
     */
    void updatePasswordHash(
            @Param("userId") String userId,
            @Param("passwordHash") String passwordHash
    );


    /**
     * 사용자 상태 변경
     */
    void updateStatus(
            @Param("userId") String userId,
            @Param("status") String status
    );
}
