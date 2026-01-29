package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.PageInfo;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;
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
    public PageResult<UserFullJoinRow> findAll(AuthStatus status, Paging paging) {
        String statusParam = (status == null) ? null : status.name();
        long totalCount = userQueryMapper.countUsers(statusParam);

        PageInfo pageInfo = PageInfo.of(paging.getPage(), paging.getSize(), totalCount);

        List<UserFullJoinRow> items = userQueryMapper.findFullJoinAll(statusParam, paging.getSize(), paging.getOffset());

        return PageResult.of(items, pageInfo);
    }

    @Override
    public long countUsers(AuthStatus status) {
        String statusParam = (status == null) ? null : status.name();
        return userQueryMapper.countUsers(statusParam);
    }
}
