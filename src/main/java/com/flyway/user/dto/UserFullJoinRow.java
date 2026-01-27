package com.flyway.user.dto;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserFullJoinRow {
    private String userId;
    private String email;
    private AuthStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime withdrawnAt;

    private AuthProvider provider;

    private String name;
    private String passportNo;
    private String country;
    private String gender;
    private String firstName;
    private String lastName;
}
