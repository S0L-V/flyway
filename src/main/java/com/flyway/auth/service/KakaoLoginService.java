package com.flyway.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface KakaoLoginService {

    /**
     * 카카오 인가 페이지로 이동
     */
    void redirectToKakao(HttpServletRequest req, HttpServletResponse res) throws IOException;

    /**
     * 카카오 콜백 처리 후 인증 완료
     */
    void handleCallback(String code, String state, HttpServletRequest req, HttpServletResponse res) throws IOException;
}
