package com.flyway.user.dto;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.domain.UserProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserProfileResponse extends UserProfile {
    private String createdAt;
    private AuthStatus status;
}
