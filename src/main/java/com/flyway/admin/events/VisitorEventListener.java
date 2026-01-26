package com.flyway.admin.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.flyway.admin.domain.VisitorLog;
import com.flyway.admin.service.VisitorLogCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorEventListener {

	private final VisitorLogCommandService visitorLogCommandService;

	@EventListener
	public void handleVisitorEvent(VisitorEvent event) {
		try {
			log.debug("Handling visitor event for session: {}", event.getSessionId());
			VisitorLog visitorLog = VisitorLog.builder()
				.sessionId(event.getSessionId())
				.userId(event.getUserId())
				.ipAddress(event.getIpAddress())
				.userAgent(event.getUserAgent())
				.pageUrl(event.getPageUrl())
				.referer(event.getReferer())
				.build();

			visitorLogCommandService.saveNewVisitorLog(visitorLog);
		} catch (Exception e) {
			log.error("Failed to process visitor event", e);
		}
	}
}
