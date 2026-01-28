package com.flyway.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.dto.PaymentListDto;
import com.flyway.admin.dto.PaymentStatsDto;
import com.flyway.admin.repository.AdminPaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

	private final AdminPaymentRepository adminPaymentRepository;

	@Override
	@Transactional(readOnly = true)
	public PaymentStatsDto getPaymentStats() {
		return PaymentStatsDto.builder()
			.paidCount(adminPaymentRepository.countPaidPayments())
			.pendingCount(adminPaymentRepository.countPendingPayments())
			.refundCount(adminPaymentRepository.countRefundedPayments())
			.monthlyRevenue(adminPaymentRepository.sumMonthlyRevenue())
			.build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentListDto> getPaymentList(String status, int page, int size) {
		int setPage = Math.max(1, page);
		int safeSize = Math.max(1, Math.min(size, 100));
		int offset = (safeSize - 1) * size;
		return adminPaymentRepository.selectPaymentList(status, offset, setPage);
	}

	@Override
	@Transactional(readOnly = true)
	public long countPayments(String status) {
		return adminPaymentRepository.countPayments(status);
	}
}
