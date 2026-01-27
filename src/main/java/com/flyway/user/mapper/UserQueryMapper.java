package com.flyway.user.mapper;

import com.flyway.user.dto.UserFullJoinRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserQueryMapper {
    /**
     * 회원 정보 단건 조회
     */
    UserFullJoinRow findFullJoinByUserId(@Param("userId") String userId);

    /**
     * 회원 정보 목록 조회
     * @param status status 기준 필터링 (null: 전체 조회)
     */
    List<UserFullJoinRow> findFullJoinAll(
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 회원 수 조회
     * @param status status 기준 필터링 (null: 전체 조회)
     */
    long countUsers(@Param("status") String status);
}
