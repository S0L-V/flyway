package com.flyway.user.service;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    @Override
    @Transactional
    public void withdraw(String userId, LocalDateTime now, HttpServletRequest request, HttpServletResponse response) {
        int updated = userRepository.markWithdrawn(userId, now);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        authTokenService.revokeAllRefreshTokens(userId, now);
        authTokenService.logout(request, response);
    }
}
