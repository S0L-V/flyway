package com.flyway.auth.service;

import com.flyway.auth.domain.KakaoToken;
import com.flyway.auth.domain.KakaoUserInfo;

public interface KakaoOAuthService {

    /**
     *  인가 코드 요청용 카카오 authorize URL 생성
     */
    String buildAuthorizeUrl(String state);

    /**
     *  인가 코드를 액세스 토큰으로 교환
     */
    KakaoToken exchangeCodeForToken(String code);

    /**
     *  액세스 토큰으로 카카오 사용자 정보를 조회한다.
     */
    KakaoUserInfo getUserInfo(String accessToken);
}
