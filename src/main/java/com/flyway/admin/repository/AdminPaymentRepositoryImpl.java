package com.flyway.admin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.PaymentListDto;
import com.flyway.admin.mapper.AdminPaymentMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminPaymentRepositoryImpl implements AdminPaymentRepository {

	private final AdminPaymentMapper adminPaymentMapper;

	@Override
	public long countPaidPayments() {
		return adminPaymentMapper.countPaidPayments();
	}

	@Override
	public long countPendingPayments() {
		return adminPaymentMapper.countPendingPayments();
	}

	@Override
	public long countRefundedPayments() {
		return adminPaymentMapper.countRefundedPayments();
	}

	@Override
	public long sumMonthlyRevenue() {
		return adminPaymentMapper.sumMonthlyRevenue();
	}

	@Override
	public List<PaymentListDto> selectPaymentList(String status, String keyword, int offset, int limit) {
		return adminPaymentMapper.selectPaymentList(status, keyword, offset, limit);
	}

	@Override
	public long countPayments(String status, String keyword) {
		return adminPaymentMapper.countPayments(status, keyword);
	}
}
