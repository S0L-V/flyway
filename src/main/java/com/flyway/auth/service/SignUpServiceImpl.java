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
        // 이메일 가입 회원 중 중복 체크
        boolean existing = userIdentityRepository.existsEmailIdentity(request.getEmail());
        if (existing) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // User 생성
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .userId(userId)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getRawPassword()))
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
}
