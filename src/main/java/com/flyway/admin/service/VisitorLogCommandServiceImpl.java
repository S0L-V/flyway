package com.flyway.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.domain.VisitorLog;
import com.flyway.admin.repository.VisitorLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitorLogCommandServiceImpl implements VisitorLogCommandService {

	private final VisitorLogRepository visitorLogRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveNewVisitorLog(VisitorLog visitorLog) {
		visitorLogRepository.save(visitorLog);
	}
}
