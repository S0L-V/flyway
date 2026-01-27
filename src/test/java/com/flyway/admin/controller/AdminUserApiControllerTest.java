package com.flyway.admin.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.flyway.template.common.PageInfo;
import com.flyway.template.common.PageResult;
import com.flyway.user.dto.UserFullJoinRow;
import com.flyway.user.service.UserQueryService;
import com.flyway.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 회원 API 컨트롤러 테스트")
class AdminUserApiControllerTest {

	@Mock
	private UserQueryService userQueryService;

	@Mock
	private UserService userService;

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
		PageResult<UserFullJoinRow> result = new PageResult<>(users, PageInfo.of(1, 20, 134));
		given(userQueryService.getUsers(null, 1, 20)).willReturn(result);

		// when & then
		mockMvc.perform(get("/admin/api/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].userId").value("user-1"))
			.andExpect(jsonPath("$.data[0].email").value("user1@test.com"))
			.andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
			.andExpect(jsonPath("$.page.page").value(1))
			.andExpect(jsonPath("$.page.size").value(20))
			.andExpect(jsonPath("$.page.totalElements").value(134))
			.andExpect(jsonPath("$.page.totalPages").value(7))
			.andExpect(jsonPath("$.page.hasNext").value(true))
			.andExpect(jsonPath("$.page.hasPrevious").value(false));

		then(userQueryService).should().getUsers(null, 1, 20);
	}

	@Test
	@DisplayName("회원 목록 조회 성공 - status 필터")
	void getUsers_success_withStatus() throws Exception {
		// given
		List<UserFullJoinRow> users = List.of(buildUser("user-2", "user2@test.com", AuthStatus.BLOCKED));
		PageResult<UserFullJoinRow> result = new PageResult<>(users, PageInfo.of(2, 5, 11));
		given(userQueryService.getUsers(AuthStatus.BLOCKED, 2, 5)).willReturn(result);

		// when & then
		mockMvc.perform(get("/admin/api/users")
				.param("status", "BLOCKED")
				.param("page", "2")
				.param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].userId").value("user-2"))
			.andExpect(jsonPath("$.data[0].status").value("BLOCKED"))
			.andExpect(jsonPath("$.page.page").value(2))
			.andExpect(jsonPath("$.page.size").value(5))
			.andExpect(jsonPath("$.page.totalElements").value(11))
			.andExpect(jsonPath("$.page.totalPages").value(3))
			.andExpect(jsonPath("$.page.hasNext").value(true))
			.andExpect(jsonPath("$.page.hasPrevious").value(true));

		then(userQueryService).should().getUsers(AuthStatus.BLOCKED, 2, 5);
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

	@Test
	@DisplayName("회원 차단 성공")
	void blockUser_success() throws Exception {
		// given
		given(userService.blockUser("user-4")).willReturn(AuthStatus.BLOCKED);

		// when & then
		mockMvc.perform(post("/admin/api/users/{userId}/block", "user-4"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.userId").value("user-4"))
			.andExpect(jsonPath("$.data.status").value("BLOCKED"))
			.andExpect(jsonPath("$.message").value("회원 차단 완료"));

		then(userService).should().blockUser("user-4");
	}

	@Test
	@DisplayName("회원 차단 해제 성공")
	void unblockUser_success() throws Exception {
		// given
		given(userService.unblockUser("user-5")).willReturn(AuthStatus.ACTIVE);

		// when & then
		mockMvc.perform(post("/admin/api/users/{userId}/unblock", "user-5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.userId").value("user-5"))
			.andExpect(jsonPath("$.data.status").value("ACTIVE"))
			.andExpect(jsonPath("$.message").value("회원 차단 해제 완료"));

		then(userService).should().unblockUser("user-5");
	}

	private UserFullJoinRow buildUser(String userId, String email, AuthStatus status) {
		UserFullJoinRow user = new UserFullJoinRow();
		user.setUserId(userId);
		user.setEmail(email);
		user.setStatus(status);
		return user;
	}
}
