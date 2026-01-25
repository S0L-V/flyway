package com.flyway.admin.service;

import org.springframework.stereotype.Service;

import com.flyway.admin.repository.VisitorLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitorLogQueryServiceImpl implements VisitorLogQueryService {

	private final VisitorLogRepository visitorLogRepository;

	@Override
	public boolean existsToday(String sessionId) {
		return visitorLogRepository.existsToday(sessionId);
	}
}
