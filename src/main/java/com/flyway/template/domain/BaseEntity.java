package com.flyway.template.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 모든 도메인 클래스의 Base 클래스
 * 공통 필드: created_at, updated_at
 */
@Getter
@Setter
public abstract class BaseEntity {
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
