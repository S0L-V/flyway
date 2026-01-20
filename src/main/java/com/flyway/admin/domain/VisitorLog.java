package com.flyway.admin.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 방문자 로그 Entity
 * 사이트 방문 추적 데이터
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorLog {

	private Long logId; // 로그 ID (AUTO_INCREMENT)
	private String sessionId; // JSESSIONID
	private String userId;
	private String ipAddress;
	private String pageUrl; // 브라우저 User-Agent
	private String referer;
	private LocalDateTime visitedAt;
}
