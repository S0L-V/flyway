package com.flyway.admin.dto;

import java.time.LocalDateTime;

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
public class AdminUserDto {

	// users 테이블
	private String userId;
	private String email;
	private String status; // ACTIVE, BLOCKED, ONBOARDING, WITHDRAWN
	private LocalDateTime createdAt;
	private LocalDateTime withdrawnAt;

	// user_profile 테이블
	private String name;
	private String firstName;
	private String lastName;
	private String gender; // M | F
	private String country;

	// user_identity 테이블
	private String provider; // KAKAO, EMAIL

	// 예약 통계 JOIN
	private int reservationCount; // 총 예약 건수

	public String getStatusDisplayName() {
		if (status == null) {
			return "";
		}
		switch (status) {
			case "ACTIVE": return "활성";
			case "BLOCKED": return "차단";
			case "ONBOARDING": return "가입중";
			case "WITHDRAWN": return "탈퇴";
			default: return status;
		}
	}

	public String getDisplayName() {
		if (name != null && !name.isEmpty()) {
			return name.trim();
		}

		String fn = firstName != null ? firstName.trim() : "";
		String ln = lastName != null ? lastName.trim() : "";
		if (!fn.isEmpty() && !ln.isEmpty()) {
			return fn + " " + ln;
		}

		if (!fn.isEmpty()) {
			return fn;
		}
		if (!ln.isEmpty()) {
			return ln;
		}
		return "-";
	}
}
