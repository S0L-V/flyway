package com.flyway.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSignUpRequest {
    private String name;
    private String email;
    private String rawPassword;
}
