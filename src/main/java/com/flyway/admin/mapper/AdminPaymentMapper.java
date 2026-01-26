package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.dto.PaymentListDto;

@Mapper
public interface AdminPaymentMapper {

	/**
	 * 결제 성공 건수
	 */
	long countPaidPayments();

	/**
	 * 승인 대기 건수 (PENDING)
	 */
	long countPendingPayments();

	/**
	 * 환불 요청 건수 (REFUNDED + CANCELLED)
	 */
	long countRefundedPayments();

	/**
	 * 이번 달 매출
	 */
	long sumMonthlyRevenue();

	/**
	 * 결제 내역 목록 조회 (페이징)
	 */
	List<PaymentListDto> selectPaymentList(
		@Param("status") String status,
		@Param("offset") int offset,
		@Param("limit") int limit
	);

	/**
	 * 결제 내역 총 건수
	 */
	long countPayments(@Param("status") String status);
}
