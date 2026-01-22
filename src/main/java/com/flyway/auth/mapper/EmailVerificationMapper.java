package com.flyway.auth.mapper;

import com.flyway.auth.domain.EmailVerificationToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface EmailVerificationMapper {

    void insertEmailVerificationToken(EmailVerificationToken token);

    EmailVerificationToken findByTokenHash(@Param("tokenHash") String tokenHash);

    int markTokenUsed(
            @Param("emailVerificationTokenId") String emailVerificationTokenId,
            @Param("usedAt") LocalDateTime usedAt
    );

    int countVerifiedByEmailPurpose(
            @Param("email") String email,
            @Param("purpose") String purpose,
            @Param("now") LocalDateTime now
    );
}
