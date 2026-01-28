package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;
import com.flyway.user.dto.UserFullJoinRow;

import java.util.Optional;

public interface UserQueryRepository {

    /**
     * 회원 정보 단건 조회
     */
    Optional<UserFullJoinRow> findByUserId(String userId);

    /**
     * 회원 정보 목록 조회
     * @param status 기준 필터링 (null: 전체 조회)
     */
    PageResult<UserFullJoinRow> findAll(AuthStatus status, Paging paging);

    /**
     * 회원 수 조회
     * @param status 기준 필터링 (null: 전체 조회)
     */
    long countUsers(AuthStatus status);
}
