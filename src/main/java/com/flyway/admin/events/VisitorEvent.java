package com.flyway.admin.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class VisitorEvent extends ApplicationEvent {

	private final String sessionId;
	private final String userId;
	private final String ipAddress;
	private final String userAgent;
	private final String pageUrl;
	private final String referer;

	public VisitorEvent(Object source, String sessionId, String userId, String ipAddress, String userAgent,
		String pageUrl, String referer) {

		super(source);
		this.sessionId = sessionId;
		this.userId = userId;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.pageUrl = pageUrl;
		this.referer = referer;
	}
}
