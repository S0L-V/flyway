package com.flyway.admin.service;

public interface VisitorLogQueryService {

	/**
	 * 해당 세션 ID로 오늘 방문한 기록이 있는지 확인
	 * @param sessionId 확인할 세션 ID
	 */
	boolean existsToday(String sessionId);
}
