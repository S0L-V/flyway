package com.flyway.auth.service;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.auth.domain.AuthStatus;
import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.repository.EmailVerificationRepository;
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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserAuthServiceImplTest {

    private UserRepository userRepository;
    private UserIdentityRepository userIdentityRepository;
    private UserProfileRepository userProfileRepository;
    private PasswordEncoder passwordEncoder;
    private SignUpAttemptRepository signUpAttemptRepository;
    private EmailVerificationRepository emailVerificationRepository;

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
    @DisplayName("중복 이메일이면 BusinessException(USER_EMAIL_ALREADY_EXISTS) 발생하고 저장 로직은 수행되지 않는다")
    void signUp_duplicateEmail_throwBusinessException() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail("dup@example.com");
        req.setRawPassword("password1234");
        req.setAttemptId("AttemptId");

        when(userIdentityRepository.existsEmailIdentity("dup@example.com"))
                .thenReturn(true);

        when(signUpAttemptRepository.consumeIfVerified(
                eq("AttemptId"),
                eq("dup@example.com"),
                any()
        )).thenReturn(1);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> signUpService.signUp(req)
        );

        // then
        assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXISTS, ex.getErrorCode());

        verify(userIdentityRepository).existsEmailIdentity("dup@example.com");
        verifyNoInteractions(userRepository);
        verify(userIdentityRepository, never()).save(any(UserIdentity.class));
        verifyNoInteractions(userProfileRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("정상 회원가입이면 User/UserIdentity/UserProfile이 저장되고, 비밀번호는 encode 된다")
    void signUp_success_saveAll() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail("test@example.com");
        req.setRawPassword("password1234");
        req.setAttemptId("AttemptId");

        when(userIdentityRepository.existsEmailIdentity("test@example.com"))
                .thenReturn(false);

        when(signUpAttemptRepository.consumeIfVerified(
                eq("AttemptId"),
                eq("test@example.com"),
                any()
        )).thenReturn(1);

        when(passwordEncoder.encode("password1234"))
                .thenReturn("ENCODED_PW");

        // when
        signUpService.signUp(req);

        // then - PasswordEncoder
        verify(passwordEncoder).encode("password1234");

        // then - User 저장값 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getUserId(), "userId는 생성되어야 함");
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("ENCODED_PW", savedUser.getPasswordHash());
        assertNotNull(savedUser.getCreatedAt(), "createdAt은 설정되어야 함");
        assertEquals(AuthStatus.ACTIVE, savedUser.getStatus());

        // then - UserIdentity 저장값 검증
        ArgumentCaptor<UserIdentity> identityCaptor = ArgumentCaptor.forClass(UserIdentity.class);
        verify(userIdentityRepository).save(identityCaptor.capture());

        UserIdentity savedIdentity = identityCaptor.getValue();
        assertNotNull(savedIdentity.getUserIdentityId(), "userIdentityId는 생성되어야 함");
        assertEquals(savedUser.getUserId(), savedIdentity.getUserId(), "User와 Identity의 userId는 같아야 함");
        assertEquals(AuthProvider.EMAIL, savedIdentity.getProvider());
        assertEquals("test@example.com", savedIdentity.getProviderUserId());
        assertNotNull(savedIdentity.getCreatedAt());

        // then - UserProfile 저장값 검증
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).createProfile(profileCaptor.capture());

        UserProfile savedProfile = profileCaptor.getValue();
        assertEquals(savedUser.getUserId(), savedProfile.getUserId());
        assertEquals("홍길동", savedProfile.getName());

        // 호출 순서 검증: User → Identity → Profile
        InOrder inOrder = inOrder(userRepository, userIdentityRepository, userProfileRepository);
        inOrder.verify(userRepository).save(any(User.class));
        inOrder.verify(userIdentityRepository).save(any(UserIdentity.class));
        inOrder.verify(userProfileRepository).createProfile(any(UserProfile.class));
    }

    @Test
    @DisplayName("비밀번호 인코딩 결과가 없으면 BusinessException(USER_PASSWORD_ENCODE_ERROR) 발생")
    void signUp_passwordEncodingMissing_throwBusinessException() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail("test@example.com");
        req.setRawPassword("password1234");
        req.setAttemptId("AttemptId");

        when(signUpAttemptRepository.consumeIfVerified(
                eq("AttemptId"),
                eq("test@example.com"),
                any()
        )).thenReturn(1);

        when(userIdentityRepository.existsEmailIdentity("test@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password1234"))
                .thenReturn(null);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> signUpService.signUp(req)
        );

        // then
        assertEquals(ErrorCode.USER_PASSWORD_ENCODE_ERROR, ex.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        verify(userIdentityRepository, never()).save(any(UserIdentity.class));
        verify(userProfileRepository, never()).createProfile(any(UserProfile.class));
    }

    @Test
    @DisplayName("이메일이 없으면 BusinessException(USER_EMAIL_REQUIRED) 발생")
    void signUp_missingEmail_throwBusinessException() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail(null);
        req.setRawPassword("password1234");

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> signUpService.signUp(req)
        );

        // then
        assertEquals(ErrorCode.USER_EMAIL_REQUIRED, ex.getErrorCode());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userIdentityRepository);
        verifyNoInteractions(userProfileRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("비밀번호가 없으면 BusinessException(USER_INVALID_INPUT) 발생")
    void signUp_missingPassword_throwBusinessException() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail("test@example.com");
        req.setRawPassword(null);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> signUpService.signUp(req)
        );

        // then
        assertEquals(ErrorCode.USER_INVALID_INPUT, ex.getErrorCode());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userIdentityRepository);
        verifyNoInteractions(userProfileRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("이름이 없으면 BusinessException(USER_INVALID_INPUT) 발생")
    void signUp_missingName_throwBusinessException() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName(null);
        req.setEmail("test@example.com");
        req.setRawPassword("password1234");

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> signUpService.signUp(req)
        );

        // then
        assertEquals(ErrorCode.USER_INVALID_INPUT, ex.getErrorCode());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userIdentityRepository);
        verifyNoInteractions(userProfileRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("existsEmailIdentity 호출은 항상 선행되어야 한다")
    void signUp_always_checks_duplicate_first() {
        // given
        EmailSignUpRequest req = new EmailSignUpRequest();
        req.setName("홍길동");
        req.setEmail("test@example.com");
        req.setRawPassword("password1234");
        req.setAttemptId("AttemptId");

        when(userIdentityRepository.existsEmailIdentity(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("ENCODED_PW");

        when(signUpAttemptRepository.consumeIfVerified(
                eq("AttemptId"),
                eq("test@example.com"),
                any()
        )).thenReturn(1);

        // when
        signUpService.signUp(req);

        // then
        InOrder inOrder = inOrder(userIdentityRepository, userRepository, passwordEncoder);
        inOrder.verify(userIdentityRepository).existsEmailIdentity("test@example.com");
    }
}
