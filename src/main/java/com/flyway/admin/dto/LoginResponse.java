package com.flyway.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.flyway.admin.domain.Role;

public class LoginResponse {
	private UUID adminId;
	private String adminName;
	private String email;
	private Role role;
	private String accessToken;
	private LocalDateTime lastLoginAt;
}
