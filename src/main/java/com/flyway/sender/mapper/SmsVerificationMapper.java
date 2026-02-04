package com.flyway.sender.mapper;

import com.flyway.sender.domain.SmsVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface SmsVerificationMapper {
    void insert(SmsVerification verification);

    SmsVerification findLatestByPhone(@Param("phoneNumber") String phoneNumber);

    int markVerified(@Param("smsVerificationId") String id,
                     @Param("verifiedAt") LocalDateTime verifiedAt);

    int countRecentByPhone(@Param("phoneNumber") String phoneNumber,
                           @Param("since") LocalDateTime since);

    // 인증 완료 여부 확인 (5분 내)
    int existsVerifiedPhone(@Param("phoneNumber") String phoneNumber,
                            @Param("since") LocalDateTime since);
}
