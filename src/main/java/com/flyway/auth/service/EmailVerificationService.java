package com.flyway.auth.service;

public interface EmailVerificationService {

    String issueSignupVerification(String email);

    String verifySignupToken(String token, String attemptId);

    boolean isSignupVerified(String email, String attemptId);
}
