package com.flyway.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 통계 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {

	private String statId;
	private String statType; // 통계 유형 DAILY, WEEKLY, MONTHLY, YEARLY
	private LocalDate statDate; // 통계 기준 날짜
	private int totalReservations; // 총 예약 건수
	private int confirmedReservations; // 확정된 예약 건수 (결제 완료)
	private int cancelledReservations; // 취소된 예약 건수
	private long totalRevenue; // 총 매출
	private long averageTicketPrice; // 평균 티켓 가격
	private long totalRefunds; // 총 환불 금액
	private int refundCount; // 환불 건수
	private int newUsers; // 신규 가입 회원 수
	private int activeUsers; // 활성 사용자 수 (방문자)
	private LocalDateTime calculatedAt; // 통계 계산 시각
}
