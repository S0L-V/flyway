package com.flyway.admin.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flyway.admin.domain.Role;
import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;
import com.flyway.admin.service.AdminAuthService;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 인증 컨트롤러 테스트")
class AdminAuthControllerTest {

	@Mock
	private AdminAuthService adminAuthService;

	@InjectMocks
	private AdminAuthController adminAuthController;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController).build();

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		session = new MockHttpSession();
	}

	@Test
	@DisplayName("로그인 페이지 표시")
	void loginPage() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/login"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name("admin/login"));
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() throws Exception {
		// given
		LoginRequest request = new LoginRequest("admin@test.com", "password123");
		LoginResponse response = LoginResponse.builder()
			.adminId("admin-id")
			.adminName("관리자")
			.email("admin@test.com")
			.role(Role.SUPER_ADMIN)
			.accessToken("jwt.token.here")
			.lastLoginAt(LocalDateTime.now())
			.build();

		given(adminAuthService.login(any(LoginRequest.class), anyString()))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/admin/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.session(session))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.adminId").value("admin-id"))
			.andExpect(jsonPath("$.data.email").value("admin@test.com"))
			.andExpect(jsonPath("$.message").value("로그인 성공"));
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호")
	void loginFail_InvalidPassword() throws Exception {
		// given
		LoginRequest request = new LoginRequest("admin@test.com", "wrongpassword");

		given(adminAuthService.login(any(LoginRequest.class), anyString()))
			.willThrow(new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED));

		// when & then
		mockMvc.perform(post("/admin/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.errorCode").value("A002"))
			.andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() throws Exception {
		// given
		session.setAttribute("adminId", "admin-id");

		// when & then
		mockMvc.perform(post("/admin/logout").session(session))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/admin/login"));

		then(adminAuthService).should().logout("admin-id");
	}
}