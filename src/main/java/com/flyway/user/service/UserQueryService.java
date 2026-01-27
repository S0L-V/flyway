package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.dto.UserFullJoinRow;

import java.util.List;

public interface UserQueryService {

    /**
     *  회원 단건 조회
     */
    UserFullJoinRow getUserDetail(String userId);

    /**
     * 회원 목록 조회
     * @param status 기준 필터랑 (null: 전체 조회)
     */
    List<UserFullJoinRow> getUsers(AuthStatus status);
}
