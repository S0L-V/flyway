package com.flyway.user.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

public interface UserWithdrawalService {

    /**
     * 회원 탈퇴
     */
    void withdraw(String userId, LocalDateTime now, HttpServletRequest request, HttpServletResponse response);

}
