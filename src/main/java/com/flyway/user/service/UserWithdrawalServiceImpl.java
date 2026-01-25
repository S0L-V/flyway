package com.flyway.user.service;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    @Override
    @Transactional
    public void withdraw(String userId, LocalDateTime now) {
        userRepository.markWithdrawn(userId, now);
        authTokenService.revokeAllRefreshTokens(userId, now);
    }
}
