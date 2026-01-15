package com.flyway.auth.controller;

import com.flyway.auth.dto.EmailSignUpRequest;
import com.flyway.auth.service.SignUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserAuthControllerTest {

    private MockMvc mockMvc;
    private SignUpService signUpService;

    @BeforeEach
    void setUp() {
        signUpService = Mockito.mock(SignUpService.class);

        AuthController controller = new AuthController(signUpService);

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

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .param("name", "홍길동")
                        .param("email", "test@example.com")
                        .param("rawPassword", "password1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(signUpService).signUp(any(EmailSignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패(중복 이메일) 시 signup 뷰로 이동하고 error 메시지 반환")
    void signUp_duplicateEmail_returnsSignupView() throws Exception {
        // given
        doThrow(new IllegalStateException("이미 가입된 이메일입니다."))
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
                .andExpect(model().attribute("error", "이미 가입된 이메일입니다."));
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
}
