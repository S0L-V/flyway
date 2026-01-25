package com.flyway.auth.controller;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.service.AuthTokenService;
import com.flyway.auth.service.KakaoLoginService;
import com.flyway.auth.service.SignUpService;
import com.flyway.security.principal.CustomUserDetails;
import com.flyway.security.service.EmailUserDetailsService;
import com.flyway.security.service.UserIdUserDetailsService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.User;
import com.flyway.user.controller.UserApiController;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.service.UserProfileService;
import com.flyway.user.service.UserWithdrawalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserAuthControllerTest {

    private MockMvc mockMvc;
    private SignUpService signUpService;
    private EmailUserDetailsService emailUserDetailsService;
    private AuthTokenService authTokenService;


    @BeforeEach
    void setUp() {
        signUpService = Mockito.mock(SignUpService.class);
        KakaoLoginService kakaoLoginService = Mockito.mock(KakaoLoginService.class);
        emailUserDetailsService = Mockito.mock(EmailUserDetailsService.class);
        UserIdUserDetailsService userIdUserDetailsService = Mockito.mock(UserIdUserDetailsService.class);
        authTokenService = Mockito.mock(AuthTokenService.class);

        AuthController controller = new AuthController(
                signUpService,
                kakaoLoginService,
                emailUserDetailsService,
                userIdUserDetailsService,
                authTokenService
        );

        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".jsp");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(vr)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 시 로그인 페이지로 리다이렉트")
    void signUp_success_redirect() throws Exception {
        // given
        doNothing().when(signUpService).signUp(any(EmailSignUpRequest.class));
        UserDetails principal = org.springframework.security.core.userdetails.User.withUsername("user-123")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        when(emailUserDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(principal);
        doNothing().when(authTokenService).issueLoginCookies(any(), any(), anyString());

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .param("name", "홍길동")
                        .param("email", "test@example.com")
                        .param("rawPassword", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(signUpService).signUp(any(EmailSignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패(중복 이메일) 시 signup 뷰로 이동하고 error 메시지 반환")
    void signUp_duplicateEmail_returnsSignupView() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS))
                .when(signUpService)
                .signUp(any(EmailSignUpRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .param("name", "홍길동")
                        .param("email", "dup@example.com")
                        .param("rawPassword", "password1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", ErrorCode.USER_EMAIL_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("회원가입 실패(필수값 누락) 시 signup 뷰로 이동하고 error 메시지 반환")
    void signUp_missingRequired_returnsSignupView() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.USER_INVALID_INPUT))
                .when(signUpService)
                .signUp(any(EmailSignUpRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .param("name", "")
                        .param("email", "")
                        .param("rawPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", ErrorCode.USER_INVALID_INPUT.getMessage()));
    }

    @Test
    @DisplayName("회원가입 실패(알 수 없는 예외) 시 signup 뷰로 이동하고 공통 error 메시지 반환")
    void signUp_unknownError_returnsSignupView() throws Exception {
        // given
        doThrow(new RuntimeException("DB down"))
                .when(signUpService)
                .signUp(any(EmailSignUpRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .param("name", "홍길동")
                        .param("email", "err@example.com")
                        .param("rawPassword", "password1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "회원가입 처리 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("GET /api/profile - 프로필 조회 성공")
    void getProfile_success() throws Exception {
        // given
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserWithdrawalService userWithdrawalService = Mockito.mock(UserWithdrawalService.class);

        UserProfileResponse response = UserProfileResponse.builder()
                .userId("user-123") // UserProfile에 userId가 있다면
                .email("test@example.com")
                .createdAt("2026-01-16T10:00:00")
                .status(null) // AuthStatus면 적절히 세팅
                .build();

        Mockito.when(userProfileService.getUserProfile(anyString()))
                .thenReturn(response);

        CustomUserDetails principal = new CustomUserDetails(
                User.builder().userId("user-123").build()
        );

        HandlerMethodArgumentResolver resolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                   @NonNull MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                   @NonNull NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return principal;
            }
        };

        UserApiController controller = new UserApiController(userProfileService, userWithdrawalService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(resolver)
                .build();

        // when & then
        mockMvc.perform(get("/api/profile")
                .header("X-USER-ID", "user-123")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.createdAt").value("2026-01-16T10:00:00"));
    }

    @Test
    @DisplayName("GET /api/profile - 인증 정보 없으면 401 반환")
    void getProfile_unauthorized_returns401() throws Exception {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserWithdrawalService userWithdrawalService = Mockito.mock(UserWithdrawalService.class);

        UserApiController controller = new UserApiController(userProfileService, userWithdrawalService);
        HandlerMethodArgumentResolver resolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                    @NonNull MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    @NonNull NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return null;
            }
        };

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(resolver)
                .build();

        mockMvc.perform(get("/api/profile")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }
}
