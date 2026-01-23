package com.flyway.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthTokenService {

    /**
     * 로그인 성공 시 access + refresh 발급/저장 및 쿠키 세팅
     */
    void issueLoginCookies(HttpServletRequest request, HttpServletResponse response, String userId);

    /**
     * refreshToken으로 access(+refresh 회전) 재발급 후 쿠키 세팅
     */
    void refresh(HttpServletRequest request, HttpServletResponse response);

    /**
     * 로그아웃: refresh 폐기 및 쿠키 삭제
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
