package com.flyway.auth.service;

import com.flyway.auth.domain.KakaoToken;
import com.flyway.auth.domain.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class KakaoOAuthServiceImpl implements KakaoOAuthService {
    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String buildAuthorizeUrl(String state) {
        String redirectUri = URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + encodedState;
    }

    public KakaoToken exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("client_secret", kakaoClientSecret);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<KakaoToken> response =
                restTemplate.exchange(
                        TOKEN_URL,
                        HttpMethod.POST,
                        request,
                        KakaoToken.class
                );

        log.debug("[KAKAO] token response status={}", response.getStatusCode());
        if (response.getBody() == null) {
            throw new IllegalStateException("Kakao token response body is null");
        }
        return response.getBody();
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response =
                restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, KakaoUserInfo.class);
        log.debug("[KAKAO] user info response status={}", response.getStatusCode());
        if (response.getBody() == null) {
            throw new IllegalStateException("Kakao user info response body is null");
        }
        return response.getBody();
    }

}
