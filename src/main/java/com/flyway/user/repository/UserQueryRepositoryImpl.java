// src/main/java/com/flyway/user/repository/UserQueryRepositoryImpl.java
package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.mapper.UserQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final UserQueryMapper userQueryMapper;

    @Override
    public Optional<UserFullJoinRow> findByUserId(String userId) {
        return Optional.ofNullable(userQueryMapper.findFullJoinByUserId(userId));
    }

    @Override
    public List<UserFullJoinRow> findAll(AuthStatus status, int limit, int offset) {
        String statusParam = (status == null) ? null : status.name();
        return userQueryMapper.findFullJoinAll(statusParam, limit, offset);
    }

    @Override
    public long countUsers(AuthStatus status) {
        String statusParam = (status == null) ? null : status.name();
        return userQueryMapper.countUsers(statusParam);
    }
}
