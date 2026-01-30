package com.flyway.user.service;

import com.flyway.auth.client.KakaoUnlinkClient;
import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.service.AuthTokenService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.UserIdentity;
import com.flyway.user.repository.UserIdentityRepository;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthTokenService authTokenService;
    private final KakaoUnlinkClient kakaoUnlinkClient;

    @Override
    @Transactional
    public void withdraw(String userId, LocalDateTime now, HttpServletRequest request, HttpServletResponse response) {
        /* 회원 상태 탈퇴로 변경 */
        int updated = userRepository.markWithdrawn(userId, now);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        /* 세션, 컨텍스트 무효화 */
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        /* Refresh Token 폐기 */
        authTokenService.revokeAllRefreshTokens(userId, now);

        /* Kakao OAuth 회원의 경우 카카오 연동 해제 (after commit) */
        String kakaoUserId = resolveKakaoUserId(userId);
        runAfterCommit(() -> tryKakaoUnlink(userId, kakaoUserId));

        /* 개인정보 익명화 처리 */
        anonymizeWithdrawnUser(userId);

        /* 로그아웃 처리 */
        authTokenService.logout(request, response);
    }

    @Override
    @Transactional
    public void anonymizeWithdrawnUser(String userId) {
        AuthProvider provider = userIdentityRepository.findByUserId(userId).getProvider();
        String anonymizedProviderUserId = "withdrawn_" + userId + "@deleted." + provider;

        int usersUpdated = userRepository.anonymizeEmailIfWithdrawn(userId, anonymizedProviderUserId);

        if (usersUpdated == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_WITHDRAWN);
        }

        var profile = userProfileRepository.findByUserId(userId);
        String userName = profile != null ? profile.getName() : null;

        /* 이름, provider_user_id 익명화  */
        userIdentityRepository.anonymizeProviderUserIdIfWithdrawn(userId, provider, anonymizedProviderUserId);
        userProfileRepository.nullifyProfileIfWithdrawn(userId, maskName(userName));
    }

    private void tryKakaoUnlink(String userId, String kakaoUserId) {
        if (kakaoUserId == null || kakaoUserId.isBlank()) {
            log.warn("kakao unlink skipped: providerUserId missing. userId={}", userId);
            return;
        }

        try {
            kakaoUnlinkClient.unlinkByKakaoUserId(kakaoUserId);
        } catch (Exception e) {
            log.warn("kakao unlink failed; continuing withdrawal. userId={}", userId, e);
        }
    }

    private String resolveKakaoUserId(String userId) {
        UserIdentity identity = userIdentityRepository.findByUserId(userId);
        if (identity == null) return null;
        if (identity.getProvider() != AuthProvider.KAKAO) return null;
        return identity.getProviderUserId();
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return null;
        if (name.length() == 1) return "*";
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
