package com.flyway.admin.repository;

import com.flyway.admin.domain.VisitorLog;

public interface VisitorLogRepository {

	/**
	 * 방문자 로그를 저장
	 */
	void save(VisitorLog visitorLog);

	/**
	 * 해당 세션 ID로 오늘 방문한 기록이 있는지 확인
	 */
	boolean existsToday(String sessionId);
}
