package com.flyway.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.flyway.admin.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
	private String adminId;
	private String adminName;
	private String email;
	private Role role;
	private String accessToken;
	private LocalDateTime lastLoginAt;
}
