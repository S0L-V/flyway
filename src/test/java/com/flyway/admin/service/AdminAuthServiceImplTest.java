package com.flyway.admin.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
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
}