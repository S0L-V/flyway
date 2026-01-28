package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.PageResult;
import com.flyway.user.dto.UserFullJoinRow;

public interface UserQueryService {

    /**
     *  회원 단건 조회
     */
    UserFullJoinRow getUserDetail(String userId);

    /**
     * 회원 목록 조회
     * @param status 기준 필터랑 (null: 전체 조회)
     */
    PageResult<UserFullJoinRow> getUsers(AuthStatus status, int page, int size);
}
