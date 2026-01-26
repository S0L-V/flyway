package com.flyway.admin.repository;

import org.springframework.stereotype.Repository;

import com.flyway.admin.domain.VisitorLog;
import com.flyway.admin.mapper.VisitorLogMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VisitorLogRepositoryImpl implements VisitorLogRepository {

	private final VisitorLogMapper visitorLogMapper;

	@Override
	public void save(VisitorLog visitorLog) {
		visitorLogMapper.insertVisitorLog(visitorLog);
	}

	@Override
	public boolean existsToday(String sessionId) {
		return visitorLogMapper.existsTodayBySessionId(sessionId) > 0;
	}
}
