package com.flyway.admin.service;

import com.flyway.admin.domain.VisitorLog;

public interface VisitorLogService {

	/**
	 * 방문자 로그를 별도의 트랜잭션에 저장
	 */
	void saveVisitorLog(VisitorLog visitorLog);

	/**
	 * 해당 세션 ID로 오늘 방문한 기록이 있는지 확인
	 */
	boolean existsToday(String sessionId);
}
