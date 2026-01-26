package com.flyway.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 방문자 상세 정보 DTO
 * 대시보드에서 일일 방문자 조회 시 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorDetailDto {

	private Long logId;
	private String sessionId;
	private String userId;
	private String userName; // users 테이블
	private String userEmail; // users 테이블
	private String ipAddress;
	private String userAgent;
	private String pageUrl;
	private String referer;
	private LocalDateTime visitedAt;
	private String browser;	// User-Agent 파싱 결과
	private String deviceType; // PC/Mobile/Tablet
}
