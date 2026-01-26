package com.flyway.user.service;

import com.flyway.auth.service.AuthTokenService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.repository.UserIdentityRepository;
import com.flyway.user.repository.UserProfileRepository;
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
    private final UserIdentityRepository userIdentityRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthTokenService authTokenService;

    private static final String DELETED_EMAIL_DOMAIN = "@deleted.local";

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

        anonymizeWithdrawnUser(userId);
        authTokenService.logout(request, response);
    }

    @Override
    @Transactional
    public void anonymizeWithdrawnUser(String userId) {
        String anonymizedEmail = "withdrawn_" + userId + DELETED_EMAIL_DOMAIN;

        int usersUpdated = userRepository.anonymizeEmailIfWithdrawn(userId, anonymizedEmail);

        if (usersUpdated == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_WITHDRAWN);
        }

        String userName = userProfileRepository.findByUserId(userId).getName();
        userIdentityRepository.anonymizeEmailProviderUserIdIfWithdrawn(userId, anonymizedEmail);
        userProfileRepository.nullifyProfileIfWithdrawn(userId, maskName(userName));
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return null;
        if (name.length() == 1) return "*";
        if (name.length() == 2) return name.charAt(0) + "*";
        return "" + name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
