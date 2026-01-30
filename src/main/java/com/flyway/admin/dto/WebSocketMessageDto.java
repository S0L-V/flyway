package com.flyway.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WebSocket 메시지 래퍼 DTO
 * 서버 <-> 클라이언트 간 메시지 포맷
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessageDto<T> {

	/**
	 * 메시지 타입
	 * - STATS: 통계 데이터
	 * - ACTIVITIES: 최근 활동
	 * - NOTIFICATIONS: 알림 목록
	 * - PING / PONG: Heartbeat
	 * - REQUEST_STATS: 통계 요청 (Client -> Server)
	 * - ERROR: 에러 메시지
	 */
	private String type;

	/**
	 * 메시지 페이로드
	 * 타입에 따라 다른 데이터 구조
	 */
	private T data;

	/**
	 * 메시지 전송 시간
	 */
	private LocalDateTime timestamp;

	/**
	 * 에러 메시지 (type=ERROR 일 때)
	 */
	private String errorMessage;

	// == factory methods ==
	public static WebSocketMessageDto<DashboardStatsDto> stats(DashboardStatsDto stats) {
		return WebSocketMessageDto.<DashboardStatsDto>builder()
			.type("STATS")
			.data(stats)
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static WebSocketMessageDto<List<RecentActivityDto>> activities(List<RecentActivityDto> activities) {
		return WebSocketMessageDto.<List<RecentActivityDto>>builder()
			.type("ACTIVITIES")
			.data(activities)
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static WebSocketMessageDto<List<AdminNotificationDto>> notifications(List<AdminNotificationDto> notifications) {
		return WebSocketMessageDto.<List<AdminNotificationDto>>builder()
			.type("NOTIFICATIONS")
			.data(notifications)
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static WebSocketMessageDto<Void> pong() {
		return WebSocketMessageDto.<Void>builder()
			.type("PONG")
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static WebSocketMessageDto<Void> error(String message) {
		return WebSocketMessageDto.<Void>builder()
			.type("ERROR")
			.errorMessage(message)
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static WebSocketMessageDto<Void> refreshAll() {
		return WebSocketMessageDto.<Void>builder()
			.type("REFRESH_ALL")
			.timestamp(LocalDateTime.now())
			.build();
	}

}
