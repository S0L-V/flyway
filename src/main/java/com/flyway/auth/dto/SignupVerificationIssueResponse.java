package com.flyway.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupVerificationIssueResponse {
    private String attemptId;
}
