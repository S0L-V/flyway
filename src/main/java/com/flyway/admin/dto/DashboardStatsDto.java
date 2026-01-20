package com.flyway.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 대시보드 통계 DTO
 * 실시간 통계 데이터를 담는 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {

	// == 오늘 통계 ==
	private long dailyVisitors; // 일일 방문자
	private long dailyReservations; // 예약 건수
	private long dailyPayments; // 결제 완료
	private long dailyCancellations; // 취소/환불
	private long dailyRevenue; // 오늘의 매출 (원)

	// == 전체 통계 ==
	private long totalUsers; // 총 회원 수
	private long activeFlights; // 운항 중 항공편

	// == 실시간 상태 ==
	private long pendingReservations; // 대기 중 예약 (HELD)
	private long pendingPayments; // 대기 중 결제 (PENDING)

	// == 알림 ==
	private long unreadNotifications; // 읽지 않은 알림 수
}
