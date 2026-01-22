package com.flyway.auth.mapper;

import com.flyway.auth.domain.SignUpAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SignUpAttemptMapper {

    int insert(SignUpAttempt attempt);

    int expirePendingAttemptsByEmail(
            @Param("email") String email,
            @Param("now") LocalDateTime now
    );

    int markVerifiedIfPending(
            @Param("attemptId") String attemptId,
            @Param("verifiedAt") LocalDateTime verifiedAt
    );

    int consumeIfVerified(
            @Param("attemptId") String attemptId,
            @Param("email") String email,
            @Param("consumedAt") LocalDateTime consumedAt
    );

    String findStatusById(@Param("attemptId") String attemptId);

    int expireIfPendingAndExpired(
            @Param("attemptId") String attemptId,
            @Param("now") LocalDateTime now
    );
}
