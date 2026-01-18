package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
import com.flyway.auth.domain.KakaoToken;
import com.flyway.auth.domain.KakaoUserInfo;
import com.flyway.security.handler.LoginSuccessHandler;
import com.flyway.security.service.UserIdUserDetailsService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.User;
import com.flyway.user.domain.UserIdentity;
import com.flyway.user.repository.UserIdentityRepository;
import com.flyway.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KakaoLoginServiceImplTest {

    private KakaoOAuthService kakaoOAuthService;
    private SignUpService signUpService;
    private UserIdUserDetailsService userIdUserDetailsService;
    private LoginSuccessHandler loginSuccessHandler;
    private UserIdentityRepository userIdentityRepository;
    private UserRepository userRepository;

    private KakaoLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        kakaoOAuthService = mock(KakaoOAuthService.class);
        signUpService = mock(SignUpService.class);
        userIdUserDetailsService = mock(UserIdUserDetailsService.class);
        loginSuccessHandler = mock(LoginSuccessHandler.class);
        userIdentityRepository = mock(UserIdentityRepository.class);
        userRepository = mock(UserRepository.class);

        service = new KakaoLoginServiceImpl(
                kakaoOAuthService,
                signUpService,
                userIdUserDetailsService,
                loginSuccessHandler,
                userIdentityRepository,
                userRepository
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("카카오 로그인 시작 시 state를 저장하고 인가 URL로 리다이렉트한다")
    void redirectToKakao_setsStateAndRedirects() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(kakaoOAuthService.buildAuthorizeUrl(anyString()))
                .thenReturn("https://kakao.example/authorize");

        service.redirectToKakao(request, response);

        MockHttpSession session = (MockHttpSession) request.getSession(false);
        assertNotNull(session);
        assertNotNull(session.getAttribute("KAKAO_OAUTH_STATE"));
        assertEquals("https://kakao.example/authorize", response.getRedirectedUrl());
    }

    @Test
    @DisplayName("state가 없거나 다르면 400 응답으로 종료된다")
    void handleCallback_invalidState_returnsBadRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.handleCallback("code", "wrong-state", request, response);

        assertEquals(400, response.getStatus());
        assertEquals("Invalid state", response.getContentAsString());
        verifyNoInteractions(kakaoOAuthService, signUpService, userIdUserDetailsService,
                loginSuccessHandler, userIdentityRepository, userRepository);
    }

    @Test
    @DisplayName("기존 카카오 사용자는 회원가입 없이 로그인 처리된다")
    void handleCallback_existingUser_loginOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true).setAttribute("KAKAO_OAUTH_STATE", "ok");

        KakaoToken token = new KakaoToken();
        token.setAccessToken("kakao-token");
        KakaoUserInfo kakaoUser = kakaoUserInfo(123L, "kakao@example.com");

        when(kakaoOAuthService.exchangeCodeForToken("code")).thenReturn(token);
        when(kakaoOAuthService.getUserInfo("kakao-token")).thenReturn(kakaoUser);

        UserIdentity identity = UserIdentity.builder()
                .userIdentityId("identity-1")
                .userId("user-1")
                .provider(AuthProvider.KAKAO)
                .providerUserId("123")
                .build();
        when(userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, "123"))
                .thenReturn(identity);

        User user = User.builder()
                .userId("user-1")
                .status(AuthStatus.ACTIVE)
                .email("kakao@example.com")
                .build();
        when(userRepository.findById("user-1")).thenReturn(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user-1")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(userIdUserDetailsService.loadUserByUsername("user-1")).thenReturn(userDetails);

        service.handleCallback("code", "ok", request, response);

        verify(signUpService, never()).signUpKakaoUser(any());
        verify(loginSuccessHandler).onAuthenticationSuccess(any(), any(), any());

        MockHttpSession session = (MockHttpSession) request.getSession(false);
        assertNotNull(session);
        assertNull(session.getAttribute("OAUTH_SIGNUP"));
        assertNull(request.getAttribute(LoginSuccessHandler.REDIRECT_PATH_ATTRIBUTE));
    }

    @Test
    @DisplayName("신규 카카오 사용자는 온보딩 정보가 세션에 저장된다")
    void handleCallback_newUser_setsOauthSignupAttributes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true).setAttribute("KAKAO_OAUTH_STATE", "ok");

        KakaoToken token = new KakaoToken();
        token.setAccessToken("kakao-token");
        KakaoUserInfo kakaoUser = kakaoUserInfo(456L, "oauth@example.com");

        when(kakaoOAuthService.exchangeCodeForToken("code")).thenReturn(token);
        when(kakaoOAuthService.getUserInfo("kakao-token")).thenReturn(kakaoUser);
        when(userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, "456"))
                .thenReturn(null);

        User user = User.builder()
                .userId("user-2")
                .status(AuthStatus.ONBOARDING)
                .email(null)
                .build();
        when(signUpService.signUpKakaoUser(kakaoUser)).thenReturn(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user-2")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(userIdUserDetailsService.loadUserByUsername("user-2")).thenReturn(userDetails);

        service.handleCallback("code", "ok", request, response);

        verify(signUpService).signUpKakaoUser(kakaoUser);
        verify(loginSuccessHandler).onAuthenticationSuccess(any(), any(), any());
        assertEquals("/signup", request.getAttribute(LoginSuccessHandler.REDIRECT_PATH_ATTRIBUTE));

        MockHttpSession session = (MockHttpSession) request.getSession(false);
        assertNotNull(session);
        assertEquals(Boolean.TRUE, session.getAttribute("OAUTH_SIGNUP"));
        assertEquals("oauth@example.com", session.getAttribute("OAUTH_SIGNUP_EMAIL"));
    }

    @Test
    @DisplayName("온보딩 상태의 기존 카카오 사용자는 회원가입 페이지로 이동한다")
    void handleCallback_existingOnboardingUser_redirectsToSignup() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true).setAttribute("KAKAO_OAUTH_STATE", "ok");

        KakaoToken token = new KakaoToken();
        token.setAccessToken("kakao-token");
        KakaoUserInfo kakaoUser = kakaoUserInfo(789L, "onboarding@example.com");

        when(kakaoOAuthService.exchangeCodeForToken("code")).thenReturn(token);
        when(kakaoOAuthService.getUserInfo("kakao-token")).thenReturn(kakaoUser);

        UserIdentity identity = UserIdentity.builder()
                .userIdentityId("identity-2")
                .userId("user-3")
                .provider(AuthProvider.KAKAO)
                .providerUserId("789")
                .build();
        when(userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, "789"))
                .thenReturn(identity);

        User user = User.builder()
                .userId("user-3")
                .status(AuthStatus.ONBOARDING)
                .email("onboarding@example.com")
                .build();
        when(userRepository.findById("user-3")).thenReturn(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user-3")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(userIdUserDetailsService.loadUserByUsername("user-3")).thenReturn(userDetails);

        service.handleCallback("code", "ok", request, response);

        verify(signUpService, never()).signUpKakaoUser(any());
        verify(loginSuccessHandler).onAuthenticationSuccess(any(), any(), any());
        assertEquals("/signup", request.getAttribute(LoginSuccessHandler.REDIRECT_PATH_ATTRIBUTE));

        MockHttpSession session = (MockHttpSession) request.getSession(false);
        assertNotNull(session);
        assertEquals(Boolean.TRUE, session.getAttribute("OAUTH_SIGNUP"));
        assertEquals("onboarding@example.com", session.getAttribute("OAUTH_SIGNUP_EMAIL"));
    }

    @Test
    @DisplayName("카카오 Identity가 있으나 User가 없으면 BusinessException(USER_INTERNAL_ERROR) 발생")
    void handleCallback_orphanIdentity_throwsBusinessException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession(true).setAttribute("KAKAO_OAUTH_STATE", "ok");

        KakaoToken token = new KakaoToken();
        token.setAccessToken("kakao-token");
        KakaoUserInfo kakaoUser = kakaoUserInfo(999L, "orphan@example.com");

        when(kakaoOAuthService.exchangeCodeForToken("code")).thenReturn(token);
        when(kakaoOAuthService.getUserInfo("kakao-token")).thenReturn(kakaoUser);

        UserIdentity identity = UserIdentity.builder()
                .userIdentityId("identity-3")
                .userId("missing-user")
                .provider(AuthProvider.KAKAO)
                .providerUserId("999")
                .build();
        when(userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, "999"))
                .thenReturn(identity);
        when(userRepository.findById("missing-user")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.handleCallback("code", "ok", request, response));

        assertEquals(ErrorCode.USER_INTERNAL_ERROR, ex.getErrorCode());
        verify(loginSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    private KakaoUserInfo kakaoUserInfo(Long id, String email) {
        KakaoUserInfo info = new KakaoUserInfo();
        info.setId(id);

        KakaoUserInfo.KakaoAccount account = new KakaoUserInfo.KakaoAccount();
        account.setEmail(email);
        info.setKakaoAccount(account);
        return info;
    }
}
