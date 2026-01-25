package com.flyway.admin.service;

import com.flyway.admin.domain.VisitorLog;

public interface VisitorLogService {

	/**
	 * 방문자 로그를 별도의 트랜잭션에 저장
	 */
	void saveVisitorLog(VisitorLog visitorLog);
}
