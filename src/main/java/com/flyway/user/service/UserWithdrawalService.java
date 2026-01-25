package com.flyway.user.service;

import java.time.LocalDateTime;

public interface UserWithdrawalService {

    /**
     * 회원 탈퇴
     */
    void withdraw(String userId, LocalDateTime now);

}
