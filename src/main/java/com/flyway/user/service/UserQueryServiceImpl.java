package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.common.PageResult;
import com.flyway.template.common.Paging;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final UserQueryRepository userQueryRepository;

    @Override
    public UserFullJoinRow getUserDetail(String userId) {
        return userQueryRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
    }

    @Override
    public PageResult<UserFullJoinRow> getUsers(AuthStatus status, Integer page, Integer size) {
        Paging paging = Paging.of(page, size, DEFAULT_PAGE, DEFAULT_SIZE, MAX_SIZE);
        return userQueryRepository.findAll(status, paging);
    }
}