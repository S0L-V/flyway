package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.dto.KakaoLoginRequest;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signUp(EmailSignUpRequest request) {
        validateRequest(request);

        // 이메일 가입 회원 중 중복 체크
        boolean existing = userIdentityRepository.existsEmailIdentity(request.getEmail());
        if (existing) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // User 생성
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

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
    public User handleKakaoLogin(KakaoLoginRequest request) {
        UserIdentity existingIdentity =
                userIdentityRepository.findByProviderUserId(AuthProvider.KAKAO, request.getKakaoUserId());

        // 이미 가입된 사용자: 회원가입 생략, 기존 사용자 식별
        if (existingIdentity != null) {
            return userRepository.findById(existingIdentity.getUserId());
        }

        // 신규 사용자 생성
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();


        User user = User.builder()
                .userId(userId)
                .email(null)
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
                .providerUserId(request.getKakaoUserId())
                .createdAt(now)
                .build();

        userIdentityRepository.save(identity);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .build();

        userProfileRepository.createProfile(profile);

        return user;
    }

    private void validateRequest(EmailSignUpRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_REQUIRED);
        }
        if (request.getRawPassword() == null || request.getRawPassword().isBlank()) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
    }
}
