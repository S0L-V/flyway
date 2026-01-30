package com.flyway.user.service;

import com.flyway.auth.client.KakaoUnlinkClient;
import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.service.AuthTokenService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.UserIdentity;
import com.flyway.user.domain.UserProfile;
import com.flyway.user.repository.UserIdentityRepository;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserWithdrawalServiceImplTest {

    private UserRepository userRepository;
    private UserIdentityRepository userIdentityRepository;
    private UserProfileRepository userProfileRepository;
    private AuthTokenService authTokenService;
    private KakaoUnlinkClient kakaoUnlinkClient;

    private UserWithdrawalServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userIdentityRepository = mock(UserIdentityRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        authTokenService = mock(AuthTokenService.class);
        kakaoUnlinkClient = mock(KakaoUnlinkClient.class);

        service = new UserWithdrawalServiceImpl(
                userRepository,
                userIdentityRepository,
                userProfileRepository,
                authTokenService,
                kakaoUnlinkClient
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원 탈퇴 시 카카오 연동 해제가 호출되고 개인정보가 익명화된다")
    void withdraw_kakaoUser_unlinksAndAnonymizes() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", "pw")
        );

        when(userRepository.markWithdrawn(userId, now)).thenReturn(1);

        UserIdentity identity = UserIdentity.builder()
                .userId(userId)
                .provider(AuthProvider.KAKAO)
                .providerUserId("12345")
                .build();
        when(userIdentityRepository.findByUserId(userId)).thenReturn(identity);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name("Alice")
                .build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(profile);

        String anonymizedEmail = "withdrawn_user-1@deleted.KAKAO";
        when(userRepository.anonymizeEmailIfWithdrawn(userId, anonymizedEmail)).thenReturn(1);
        when(userIdentityRepository.anonymizeProviderUserIdIfWithdrawn(userId, AuthProvider.KAKAO, anonymizedEmail))
                .thenReturn(1);
        when(userProfileRepository.nullifyProfileIfWithdrawn(userId, "A***e")).thenReturn(1);

        service.withdraw(userId, now, request, response);

        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(authTokenService).revokeAllRefreshTokens(userId, now);
        verify(kakaoUnlinkClient).unlinkByKakaoUserId("12345");
        verify(userRepository).anonymizeEmailIfWithdrawn(userId, anonymizedEmail);
        verify(userIdentityRepository).anonymizeProviderUserIdIfWithdrawn(userId, AuthProvider.KAKAO, anonymizedEmail);
        verify(userProfileRepository).nullifyProfileIfWithdrawn(userId, "A***e");
        verify(authTokenService).logout(request, response);
    }

    @Test
    @DisplayName("이미 탈퇴 처리된 사용자는 예외가 발생한다")
    void withdraw_alreadyWithdrawn_throws() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(userRepository.markWithdrawn(userId, now)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.withdraw(userId, now, request, response));

        assertEquals(ErrorCode.USER_ALREADY_WITHDRAWN, exception.getErrorCode());
        verifyNoInteractions(userIdentityRepository, userProfileRepository, authTokenService, kakaoUnlinkClient);
    }
}
