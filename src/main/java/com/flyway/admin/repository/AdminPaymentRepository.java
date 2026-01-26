package com.flyway.admin.repository;

import java.util.List;

import com.flyway.admin.dto.PaymentListDto;

public interface AdminPaymentRepository {

	long countPaidPayments();

	long countPendingPayments();

	long countRefundedPayments();

	long sumMonthlyRevenue();

	List<PaymentListDto> selectPaymentList(String status, int offset, int limit);

	long countPayments(String status);
}
