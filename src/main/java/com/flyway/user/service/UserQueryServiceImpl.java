// src/main/java/com/flyway/user/service/UserQueryService.java
package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserQueryRepository userQueryRepository;

    @Override
    public UserFullJoinRow getUserDetail(String userId) {
        return userQueryRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
    }

    @Override
    public List<UserFullJoinRow> getUsers(AuthStatus status) {
        return userQueryRepository.findAll(status);
    }
}
