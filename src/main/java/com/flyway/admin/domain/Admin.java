package com.flyway.admin.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.flyway.template.domain.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {
	private String adminId;	// 관리자 ID
	private String passwordHash;	// 관리자 비밀번호 해시
	private String email; 	// 관리자 이메일
	private String adminName;	// 관리자 이름
	private String phone;	// 관리자 연락처
	private Role role; 		// 관리자 역할
	private String isActive;		// 관리자 계정 활성 여부
	private LocalDateTime lastLoginAt;		// 마지막 로그인 날짜
	private String lastLoginIp;			// 마지막 로그인 IP
	private LocalDateTime passwordChangedAt;	// 비밀번호 변경일
	private Integer failedLoginCount;		// 로그인 실패 횟수
	private LocalDateTime lockedUntil;		// 계정 잠금 해제 시각
	private String createdBy;		// 생성자 (관리자 ID)
	private String updatedBy;		// 수정자
}
