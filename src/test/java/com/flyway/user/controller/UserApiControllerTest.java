package com.flyway.user.controller;

import com.flyway.security.principal.CustomUserDetails;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.User;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.service.UserProfileService;
import com.flyway.user.service.UserWithdrawalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiControllerTest {

    @Test
    @DisplayName("PATCH /api/profile - 프로필 수정 성공(부분 업데이트)")
    void updateProfile_success() throws Exception {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserWithdrawalService userWithdrawalService = Mockito.mock(UserWithdrawalService.class);

        UserProfileResponse response = UserProfileResponse.builder()
                .userId("user-123")
                .gender("F")
                .build();

        when(userProfileService.updateProfile(anyString(), any()))
                .thenReturn(response);

        MockMvc mockMvc = buildMockMvc(userProfileService, userWithdrawalService,
                new CustomUserDetails(User.builder().userId("user-123").build()));

        mockMvc.perform(patch("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gender\":\"F\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gender").value("F"));
    }

    @Test
    @DisplayName("PATCH /api/profile - 인증 정보 없으면 401 반환")
    void updateProfile_unauthorized_returns401() throws Exception {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserWithdrawalService userWithdrawalService = Mockito.mock(UserWithdrawalService.class);

        MockMvc mockMvc = buildMockMvc(userProfileService, userWithdrawalService, null);

        mockMvc.perform(patch("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gender\":\"F\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("PATCH /api/profile - 입력 오류면 400 반환")
    void updateProfile_invalidInput_returns400() throws Exception {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserWithdrawalService userWithdrawalService = Mockito.mock(UserWithdrawalService.class);

        doThrow(new BusinessException(ErrorCode.USER_INVALID_INPUT))
                .when(userProfileService)
                .updateProfile(anyString(), any());

        MockMvc mockMvc = buildMockMvc(userProfileService, userWithdrawalService,
                new CustomUserDetails(User.builder().userId("user-123").build()));

        mockMvc.perform(patch("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gender\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.USER_INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_INVALID_INPUT.getMessage()));
    }

    private MockMvc buildMockMvc(UserProfileService userProfileService,
                                 UserWithdrawalService userWithdrawalService,
                                 CustomUserDetails principal) {
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
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(resolver)
                .build();
    }
}
