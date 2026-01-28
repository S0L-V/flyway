package com.flyway.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDto {

	private long paidCount; // 결제 성공 건수
	private long pendingCount; // 승인 대기 건수
	private long refundCount; // 환불 요청 건수
	private long monthlyRevenue; // 이번 달 매출
}
