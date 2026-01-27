package com.flyway.admin.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.service.UserQueryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 회원 API 컨트롤러 테스트")
class AdminUserApiControllerTest {

	@Mock
	private UserQueryService userQueryService;

	@InjectMocks
	private AdminUserApiController controller;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	@DisplayName("회원 목록 조회 성공 - status 없음")
	void getUsers_success_noStatus() throws Exception {
		// given
		List<UserFullJoinRow> users = List.of(buildUser("user-1", "user1@test.com", AuthStatus.ACTIVE));
		given(userQueryService.getUsers(null)).willReturn(users);

		// when & then
		mockMvc.perform(get("/admin/api/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].userId").value("user-1"))
			.andExpect(jsonPath("$.data[0].email").value("user1@test.com"))
			.andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

		then(userQueryService).should().getUsers(null);
	}

	@Test
	@DisplayName("회원 목록 조회 성공 - status 필터")
	void getUsers_success_withStatus() throws Exception {
		// given
		List<UserFullJoinRow> users = List.of(buildUser("user-2", "user2@test.com", AuthStatus.BLOCKED));
		given(userQueryService.getUsers(AuthStatus.BLOCKED)).willReturn(users);

		// when & then
		mockMvc.perform(get("/admin/api/users").param("status", "BLOCKED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].userId").value("user-2"))
			.andExpect(jsonPath("$.data[0].status").value("BLOCKED"));

		then(userQueryService).should().getUsers(AuthStatus.BLOCKED);
	}

	@Test
	@DisplayName("회원 단건 조회 성공")
	void getUserDetail_success() throws Exception {
		// given
		UserFullJoinRow user = buildUser("user-3", "user3@test.com", AuthStatus.ONBOARDING);
		given(userQueryService.getUserDetail("user-3")).willReturn(user);

		// when & then
		mockMvc.perform(get("/admin/api/users/{userId}", "user-3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.userId").value("user-3"))
			.andExpect(jsonPath("$.data.email").value("user3@test.com"))
			.andExpect(jsonPath("$.data.status").value("ONBOARDING"));

		then(userQueryService).should().getUserDetail("user-3");
	}

	private UserFullJoinRow buildUser(String userId, String email, AuthStatus status) {
		UserFullJoinRow user = new UserFullJoinRow();
		user.setUserId(userId);
		user.setEmail(email);
		user.setStatus(status);
		return user;
	}
}
