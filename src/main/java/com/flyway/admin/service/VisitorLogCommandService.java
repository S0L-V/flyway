package com.flyway.admin.service;

import com.flyway.admin.domain.VisitorLog;

public interface VisitorLogCommandService {

	/**
	 * 방문자 로그를 새로운 트랜잭션으로 저장
	 * @param visitorLog 저장할 로그 정보
	 */
	void saveNewVisitorLog(VisitorLog visitorLog);
}
