package com.flyway.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 최근 활동 DTO
 * 예약/결제/환불 등의 최근 활동 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {

	private String activityId; // 활동 ID
	private String activityType; // 활동 유형 (RESERVATION, PAYMENT, REFUND, CANCELLATION)
	private String description; // 설명
	private String userName;
	private String userEmail;
	private Long amount; // 금액 (nullable)
	private String status; // 상태 (COMPLETED, PENDING, FAILED)
	private LocalDateTime createdAt;

	// 추가
	private String reservationId; // 예약 ID
	private String flightNumber; // 항공편 번호

}
