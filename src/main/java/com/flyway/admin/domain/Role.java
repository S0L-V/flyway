package com.flyway.admin.domain;

import lombok.Getter;

@Getter
public enum Role {
	SUPER_ADMIN("슈퍼관리자", "모든 권한"),
	ADMIN("관리자", "일반 관리 업무"),
	VIEWER("뷰어", "조회만 가능");

	private final String displayName;
	private final String description;

	Role(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}
}
