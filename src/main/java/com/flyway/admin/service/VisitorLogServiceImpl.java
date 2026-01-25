package com.flyway.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.domain.VisitorLog;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitorLogServiceImpl implements VisitorLogService {

	private final VisitorLogService visitorLogService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveVisitorLog(VisitorLog visitorLog) {
		visitorLogService.saveVisitorLog(visitorLog);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsToday(String sessionId) {
		return visitorLogService.existsToday(sessionId);
	}
}
