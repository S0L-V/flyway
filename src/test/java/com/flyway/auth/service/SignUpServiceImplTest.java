package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SignUpServiceImplTest {

    private UserRepository userRepository;
    private UserIdentityRepository userIdentityRepository;
    private UserProfileRepository userProfileRepository;
    private SignUpAttemptRepository signUpAttemptRepository;
    private PasswordEncoder passwordEncoder;
    private SignUpServiceImpl signUpService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userIdentityRepository = Mockito.mock(UserIdentityRepository.class);
        userProfileRepository = Mockito.mock(UserProfileRepository.class);
        signUpAttemptRepository = Mockito.mock(SignUpAttemptRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);

        signUpService = new SignUpServiceImpl(
                userRepository,
                userIdentityRepository,
                userProfileRepository,
                signUpAttemptRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자/아이덴티티/프로필이 생성된다")
    void signUp_success_createsUserAndIdentity() {
        EmailSignUpRequest request = EmailSignUpRequest.builder()
                .name("Tester")
                .email("test@example.com")
                .rawPassword("password")
                .attemptId("attempt-1")
                .build();

        when(signUpAttemptRepository.consumeIfVerified(eq("attempt-1"), eq("test@example.com"), any()))
                .thenReturn(1);
        when(userIdentityRepository.existsEmailIdentity("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        signUpService.signUp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded");
        assertThat(savedUser.getStatus()).isEqualTo(AuthStatus.ACTIVE);

        ArgumentCaptor<UserIdentity> identityCaptor = ArgumentCaptor.forClass(UserIdentity.class);
        verify(userIdentityRepository).save(identityCaptor.capture());
        UserIdentity savedIdentity = identityCaptor.getValue();
        assertThat(savedIdentity.getProvider()).isEqualTo(AuthProvider.EMAIL);
        assertThat(savedIdentity.getProviderUserId()).isEqualTo("test@example.com");

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).createProfile(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getName()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("인증 시도 정보가 유효하지 않으면 회원가입이 실패한다")
    void signUp_invalidAttempt_throws() {
        EmailSignUpRequest request = EmailSignUpRequest.builder()
                .name("Tester")
                .email("test@example.com")
                .rawPassword("password")
                .attemptId("attempt-1")
                .build();

        when(signUpAttemptRepository.consumeIfVerified(anyString(), anyString(), any()))
                .thenReturn(0);

        assertThatThrownBy(() -> signUpService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_INVALID_SIGN_UP_ATTEMPT.getMessage());

        verifyNoInteractions(userRepository, userIdentityRepository, userProfileRepository);
    }

    @Test
    @DisplayName("이메일이 이미 존재하면 회원가입이 실패한다")
    void signUp_duplicateEmail_throws() {
        EmailSignUpRequest request = EmailSignUpRequest.builder()
                .name("Tester")
                .email("test@example.com")
                .rawPassword("password")
                .attemptId("attempt-1")
                .build();

        when(signUpAttemptRepository.consumeIfVerified(eq("attempt-1"), eq("test@example.com"), any()))
                .thenReturn(1);
        when(userIdentityRepository.existsEmailIdentity("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> signUpService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_EMAIL_ALREADY_EXISTS.getMessage());

        verifyNoInteractions(userRepository, userProfileRepository);
    }

    @Test
    @DisplayName("비밀번호 인코딩 실패 시 회원가입이 실패한다")
    void signUp_passwordEncodeFails_throws() {
        EmailSignUpRequest request = EmailSignUpRequest.builder()
                .name("Tester")
                .email("test@example.com")
                .rawPassword("password")
                .attemptId("attempt-1")
                .build();

        when(signUpAttemptRepository.consumeIfVerified(eq("attempt-1"), eq("test@example.com"), any()))
                .thenReturn(1);
        when(userIdentityRepository.existsEmailIdentity("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> signUpService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_PASSWORD_ENCODE_ERROR.getMessage());

        verifyNoInteractions(userRepository, userProfileRepository);
    }
}
