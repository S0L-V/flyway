package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
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
        HttpSession pre = req.getSession(false);
        String saved = (pre != null) ? (String) pre.getAttribute(KAKAO_STATE_ATTR) : null;

        if (pre == null || !Objects.equals(saved, state)) {
            res.setStatus(400);
            res.getWriter().write("Invalid state");
            return;
        }

        pre.removeAttribute(KAKAO_STATE_ATTR);

        KakaoToken token = kakaoOAuthService.exchangeCodeForToken(code);
        KakaoUserInfo userInfo = kakaoOAuthService.getUserInfo(token.getAccessToken());

        User user = resolveUser(userInfo);

        UserDetails userDetails =
                userIdUserDetailsService.loadUserByUsername(user.getUserId());
        UsernamePasswordAuthenticationToken auth = createAuthentication(userDetails);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        pre.invalidate();
        req.getSession(true);

        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, req, res);

        loginSuccessHandler.onAuthenticationSuccess(req, res, auth);
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
            return userRepository.findById(existingIdentity.getUserId());
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

}
