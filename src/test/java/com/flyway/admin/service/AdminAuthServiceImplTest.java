package com.flyway.admin.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.flyway.admin.domain.Admin;
import com.flyway.admin.domain.Role;
import com.flyway.admin.dto.LoginRequest;
import com.flyway.admin.dto.LoginResponse;
import com.flyway.admin.repository.AdminRepository;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.util.AdminJwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("관리자 인증 서비스 테스트")
class AdminAuthServiceImplTest {

	@Mock
	private AdminRepository adminRepository;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	@Mock
	private AdminJwtUtil adminJwtUtil;

	@InjectMocks
	private AdminAuthServiceImpl adminAuthService;

	private Admin testAdmin;
	private LoginRequest loginRequest;

	@BeforeEach
	void setUp() {
		// 테스트용 관리자 데이터
		testAdmin = Admin.builder()
			.adminId("test-admin-id")
			.email("admin@test.com")
			.passwordHash("$2a$10$hashedPassword")
			.adminName("테스트 관리자")
			.role(Role.SUPER_ADMIN)
			.isActive("Y")
			.failedLoginCount(0)
			.build();

		loginRequest = new LoginRequest();
		loginRequest.setEmail("admin@test.com");
		loginRequest.setPassword("password123");
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() {
		// given
		String ipAddress = "192.168.1.1";
		String expectedToken = "jwt.token.here";

		given(adminRepository.findByEmail(anyString())).willReturn(testAdmin);
		given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
		given(adminJwtUtil.generateToken(anyString(), anyString(), anyString())).willReturn(expectedToken);

		// when
		LoginResponse res = adminAuthService.login(loginRequest, ipAddress);

		// then
		assertThat(res).isNotNull();
		assertThat(res.getAdminId()).isEqualTo("test-admin-id");
		assertThat(res.getEmail()).isEqualTo("admin@test.com");
		assertThat(res.getRole()).isEqualTo(Role.SUPER_ADMIN);
		assertThat(res.getAccessToken()).isEqualTo(expectedToken);

		// 메서드 호출 검증
		then(adminRepository).should().handleLoginSuccess("test-admin-id");
		then(adminRepository).should().updateLoginInfo("test-admin-id", ipAddress);
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 관리자")
	void loginFail_AdminNotFound() {
		// given
		given(adminRepository.findByEmail(anyString())).willReturn(null);

		// when & then
		assertThatThrownBy(() -> adminAuthService.login(loginRequest, "192.168.1.1"))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED);
	}

	@Test
	@DisplayName("로그인 실패 - 비활성화 계정")
	void loginFail_InactiveAccount() {
		// given
		testAdmin.setIsActive("N");
		given(adminRepository.findByEmail(anyString())).willReturn(testAdmin);

		// when & then
		assertThatThrownBy(() -> adminAuthService.login(loginRequest, "192.168.1.1"))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_ACCOUNT_INACTIVE);
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호")
	void loginFail_InvalidPassword() {
		// given
		given(adminRepository.findByEmail(anyString())).willReturn(testAdmin);
		given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> adminAuthService.login(loginRequest, "192.168.1.1"))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED);

		// 실패 처리 메서드 호출 검증
		then(adminRepository).should().handleLoginFailure("test-admin-id");
	}

	@Test
	@DisplayName("로그아웃")
	void logout() {
		// given
		String adminId = "test-admin-id";

		// when
		adminAuthService.logout(adminId);

		// then
		assertThatCode(() -> adminAuthService.logout(adminId))
			.doesNotThrowAnyException();
	}
}