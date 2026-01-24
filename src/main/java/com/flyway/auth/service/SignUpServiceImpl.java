package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
import com.flyway.auth.domain.KakaoUserInfo;
import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.repository.SignUpAttemptRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.User;
import com.flyway.user.domain.UserIdentity;
import com.flyway.user.domain.UserProfile;
import com.flyway.user.repository.UserIdentityRepository;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserProfileRepository userProfileRepository;
    private final SignUpAttemptRepository signUpAttemptRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signUp(EmailSignUpRequest request) {
        validateRequest(request);
        LocalDateTime now = LocalDateTime.now();

        int validAttempt = signUpAttemptRepository.consumeIfVerified(request.getAttemptId(), request.getEmail(), now);
        if (validAttempt != 1) {
            throw new BusinessException(ErrorCode.USER_INVALID_SIGN_UP_ATTEMPT);
        }

        boolean existing = userIdentityRepository.existsEmailIdentity(request.getEmail());
        if (existing) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        String userId = UUID.randomUUID().toString();

        String encodedPassword;
        try {
            encodedPassword = passwordEncoder.encode(request.getRawPassword());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ENCODE_ERROR);
        }

        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ENCODE_ERROR);
        }

        User user = User.builder()
                .userId(userId)
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .status(AuthStatus.ACTIVE)
                .createdAt(now)
                .build();

        userRepository.save(user);

        // UserIdentity 생성
        UserIdentity identity = UserIdentity.builder()
                .userIdentityId(UUID.randomUUID().toString())
                .userId(userId)
                .provider(AuthProvider.EMAIL)
                .providerUserId(request.getEmail())
                .createdAt(now)
                .build();

        userIdentityRepository.save(identity);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name(request.getName())
                .build();

        userProfileRepository.createProfile(profile);
    }

    @Override
    @Transactional
    public User signUpKakaoUser(KakaoUserInfo userInfo) {
        validateKakaoUserInfo(userInfo);
        String kakaoUserId = String.valueOf(userInfo.getId());
        KakaoUserInfo.KakaoAccount account = userInfo.getKakaoAccount();
        String email = (account != null) ? account.getEmail() : null;
        String nickname = (account != null && account.getProfile() != null)
                ? account.getProfile().getNickname()
                : null;

        // 신규 사용자 생성
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .userId(userId)
                .email(email)
                .passwordHash(null)
                .status(AuthStatus.ONBOARDING)
                .createdAt(now)
                .build();

        userRepository.save(user);

        // Identity 생성
        UserIdentity identity = UserIdentity.builder()
                .userIdentityId(UUID.randomUUID().toString())
                .userId(userId)
                .provider(AuthProvider.KAKAO)
                .providerUserId(kakaoUserId)
                .createdAt(now)
                .build();

        userIdentityRepository.save(identity);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name(nickname)
                .build();

        userProfileRepository.createProfile(profile);

        return user;
    }

    @Override
    @Transactional
    public void completeOauthSignUp(String userId, EmailSignUpRequest request) {
        validateOauthRequest(request);
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }

        User user = userRepository.findById(userId);
        if (user == null || user.getStatus() != AuthStatus.ONBOARDING) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }

        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            userRepository.updateEmail(userId, email);
        }

        userRepository.updateStatus(userId, AuthStatus.ACTIVE);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name(request.getName())
                .build();
        userProfileRepository.updateProfile(profile);
    }

    private void validateRequest(EmailSignUpRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        requireText(request.getEmail(), ErrorCode.USER_EMAIL_REQUIRED);
        requireText(request.getRawPassword(), ErrorCode.USER_INVALID_INPUT);
        requireText(request.getName(), ErrorCode.USER_INVALID_INPUT);
        requireText(request.getAttemptId(), ErrorCode.USER_INVALID_SIGN_UP_ATTEMPT);
    }

    private void validateOauthRequest(EmailSignUpRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        requireText(request.getEmail(), ErrorCode.USER_EMAIL_REQUIRED);
        requireText(request.getName(), ErrorCode.USER_INVALID_INPUT);
    }

    private void validateKakaoUserInfo(KakaoUserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
    }

    private void requireText(String value, ErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
    }
}
