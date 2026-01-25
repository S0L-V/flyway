package com.flyway.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.domain.VisitorLog;
import com.flyway.admin.mapper.VisitorLogMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitorLogServiceImpl implements VisitorLogService {

	private final VisitorLogMapper visitorLogMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveVisitorLog(VisitorLog visitorLog) {
		visitorLogMapper.insertVisitorLog(visitorLog);
	}
}
