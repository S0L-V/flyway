package com.flyway.admin.service;

import java.util.List;

import com.flyway.admin.dto.PaymentListDto;
import com.flyway.admin.dto.PaymentStatsDto;

public interface AdminPaymentService {

	/**
	 * 관리자 대시보드 결제 통계 조회
	 */
	PaymentStatsDto getPaymentStats();

	/**
	 * 결제 내역 목록 조회 (페이징 포함)
	 * @param status PAID, PENDING
	 * @param keyword 이름/이메일 검색어
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지당 항목 수
	 */
	List<PaymentListDto> getPaymentList(String status, String keyword, int page, int size);

	/**
	 * 특정 상태의 결제 내역 총 건수 조회
	 * @param status 필터링할 결제 상태
	 * @param keyword 이름/이메일 검색어
	 */
	long countPayments(String status, String keyword);
}
