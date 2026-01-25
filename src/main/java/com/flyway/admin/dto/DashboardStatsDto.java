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

	// == 기간별 통계 (첫 번째 줄) ==
	private long dailyVisitors; // 방문자
	private long dailyPayments; // 결제 완료
	private long dailyCancellations; // 취소/환불
	private long dailyRevenue; // 오늘의 매출 (원)

	// == 실시간/전체 통계 (두 번째 줄) ==
	private long pendingReservations; // 대기 중 예약 (HELD - 결제 대기)
	private long totalUsers; // 총 회원 수
	private long dailyNewUsers; // 신규 가입
	private long activeFlights; // 운항 중 항공편

	// == 알림 ==
	private long unreadNotifications; // 읽지 않은 알림 수
}
