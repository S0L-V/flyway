package com.flyway.user.domain;

import com.flyway.auth.domain.AuthProvider;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentity {
    private String userIdentityId;
    private String userId;
    private AuthProvider provider;
    private String providerUserId;
    private LocalDateTime createdAt;
}