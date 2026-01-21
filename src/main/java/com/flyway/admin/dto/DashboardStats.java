package com.flyway.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

	// == 오늘 통계 ==
	private long dailyVisitors; // 일일 방문자
	private long dailyReservations; // 예약 건수
	private long dailyPayments; // 결제 완료
	private long dailyCancellations; // 최소/환불
	private long dailyRevenue; // 오늘의 매출(원)

	// == 전체 통계 ==
	private long totalUsers; // 총 회원 수
	private long activeFlights; // 운항 중 항공편

	// == 실시간 상태 ==
	private long pendingReservations; // 대기 중 예약 (HELD)
	private long pendingPayments; // 대기 중 결제 (PENDING)

	// == 알림 ==
	private long unreadNotifications; // 읽지 않은 알림 수
}
