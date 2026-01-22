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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements KakaoLoginService {

    private static final String KAKAO_STATE_ATTR = "KAKAO_OAUTH_STATE";
    private static final String OAUTH_SIGNUP_FLAG_ATTR = "OAUTH_SIGNUP";
    private static final String OAUTH_SIGNUP_EMAIL_ATTR = "OAUTH_SIGNUP_EMAIL";
    private final KakaoOAuthService kakaoOAuthService;
    private final SignUpService signUpService;
    private final UserIdUserDetailsService userIdUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final UserIdentityRepository userIdentityRepository;
    private final UserRepository userRepository;

    @Override
    public void redirectToKakao(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String state = UUID.randomUUID().toString();
        req.getSession().setAttribute(KAKAO_STATE_ATTR, state);

        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);
        res.sendRedirect(authorizeUrl);
    }

    @Override
    public void handleCallback(
            String code,
            String state,
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {
        HttpSession loginSession = validateStateOrFail(req, state, res);
        if (loginSession == null) {
            return;
        }

        KakaoUserInfo kakaoUser = fetchKakaoUser(code);

        User user = resolveUser(kakaoUser);
        setRedirectIfOnboarding(req, user);

        UsernamePasswordAuthenticationToken auth = authenticate(user);

        rotateSession(loginSession, req); // Session fixation 방지: 로그인 전 세션 폐기 후 새 세션 발급

        setOauthSignupAttributes(req, user, kakaoUser); // 온보딩에 필요한 값 세션/요청에 저장

        saveSecurityContext(req, res, auth); // 세션에 SecurityContext 저장

        loginSuccessHandler.onAuthenticationSuccess(req, res, auth);  // 토큰 쿠키 발급/리다이렉트 등 후처리
    }

    /**
     * OAuth 콜백 요청의 state 값을 검증하여 CSRF 공격 및 위조된 인증 요청 방지
     */
    private HttpSession validateStateOrFail(
            HttpServletRequest req,
            String state,
            HttpServletResponse res
    ) throws IOException {
        HttpSession session = req.getSession(false);
        String saved = (session != null) ? (String) session.getAttribute(KAKAO_STATE_ATTR) : null;

        if (session == null || !Objects.equals(saved, state)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid state");
            return null;
        }
        session.removeAttribute(KAKAO_STATE_ATTR);
        return session;
    }

    /**
     * OAuth 인가 코드(code)를 사용하여 카카오 액세스 토큰을 발급받고 사용자 정보를 조회
     */
    private KakaoUserInfo fetchKakaoUser(String code) {
        KakaoToken token = kakaoOAuthService.exchangeCodeForToken(code);
        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.USER_INTERNAL_ERROR);
        }
        return kakaoOAuthService.getUserInfo(token.getAccessToken());
    }

    /**
     * 신규 OAuth 가입자(온보딩 상태)인 경우 로그인 성공 후 회원가입 페이지로 리다이렉트되도록 경로를 설정
     */
    private void setRedirectIfOnboarding(HttpServletRequest req, User user) {
        if (AuthStatus.ONBOARDING.equals(user.getStatus())) {
            req.setAttribute(LoginSuccessHandler.REDIRECT_PATH_ATTRIBUTE, "/signup");
        }
    }

    /**
     * 서비스의 User 정보를 기반으로 Spring Security 인증 객체(Authentication)를 생성
     */
    private UsernamePasswordAuthenticationToken authenticate(User user) {
        UserDetails userDetails = userIdUserDetailsService.loadUserByUsername(user.getUserId());
        return createAuthentication(userDetails);
    }

    /**
     * 세션 고정(Session Fixation) 공격을 방지하기 위해 기존 세션을 무효화하고 새로운 세션을 생성
     */
    private void rotateSession(HttpSession oldSession, HttpServletRequest req) {
        if (oldSession != null) oldSession.invalidate();
        req.getSession(true);
    }

    /**
     * 인증 정보를 SecurityContext에 설정하고 이를 세션에 저장하여 이후 요청에서도 로그인 상태를 유지
     */
    private void saveSecurityContext(HttpServletRequest req, HttpServletResponse res,
                                     UsernamePasswordAuthenticationToken auth) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        securityContextRepository().saveContext(context, req, res);
    }

    private HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * UserDetails로 인증 토큰 생성
     */
    private UsernamePasswordAuthenticationToken createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    /**
     * 카카오 계정 기준으로 기존 사용자 여부를 확인 후 필요한 경우 가입 진행
     */
    private User resolveUser(KakaoUserInfo userInfo) {
        String kakaoUserId = extractKakaoUserId(userInfo);
        UserIdentity existingIdentity =
                userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, kakaoUserId);

        if (existingIdentity != null) {
            User user = userRepository.findById(existingIdentity.getUserId());
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_INTERNAL_ERROR);
            }
            return user;
        }

        return signUpService.signUpKakaoUser(userInfo);
    }

    /**
     * 카카오 사용자 식별자 추출 후 필수값 검증
     */
    private String extractKakaoUserId(KakaoUserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        return String.valueOf(userInfo.getId());
    }

    /**
     * OAuth 회원가입 대상인 경우 이메일을 세션에 저장해 회원가입 화면에서 사용
     */
    private void setOauthSignupAttributes(HttpServletRequest req, User user, KakaoUserInfo userInfo) {
        if (!AuthStatus.ONBOARDING.equals(user.getStatus())) {
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute(OAUTH_SIGNUP_FLAG_ATTR, Boolean.TRUE);

        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            email = extractEmail(userInfo);
        }

        if (email != null && !email.isBlank()) {
            session.setAttribute(OAUTH_SIGNUP_EMAIL_ATTR, email);
        }
    }

    /**
     * 카카오 사용자 정보에서 이메일 추출
     */
    private String extractEmail(KakaoUserInfo userInfo) {
        if (userInfo == null || userInfo.getKakaoAccount() == null) {
            return null;
        }
        return userInfo.getKakaoAccount().getEmail();
    }

}
