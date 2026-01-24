package com.flyway.admin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cglib.core.Local;

import com.flyway.admin.dto.StatisticsDto;

public interface StatisticsRepository {

	// ========== 통계 CRUD ===========
	/**
	 * 통계 저장 (UPSERT)
	 */
	int saveStatistics(StatisticsDto statistics);

	/**
	 * 특정 날짜/유형의 통계 조회
	 */
	StatisticsDto findStatistics(String statType, LocalDate statDate);

	/**
	 * 기간별 통계 목록 조회
	 */
	List<StatisticsDto> findStatisticsByPeriod(String statType, LocalDate startDate, LocalDate endDate);

	/**
	 * 최근 N일간 일일 통계 조회
	 */
	List<StatisticsDto> findRecentDailyStatistics(int days);

	// ========== 통계 계산용 (원본 테이블에서 집계) ===========

	/**
	 * 특정 기간의 총 예약 건수
	 */
	int countReservationsByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 확정 예약 건수 (결제 완료)
	 */
	int countConfirmedReservationsByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 취소 예약 건수
	 */
	int countCancelledReservationsByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 총 매출
	 */
	long sumRevenueByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 평균 티켓 가격
	 */
	long avgTicketPriceByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 환불 건수
	 */
	int countRefundsByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 총 환불 금액
	 */
	long sumRefundsByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 신규 가입 회원 수
	 */
	int countNewUsersByPeriod(LocalDate startDate, LocalDate endDate);

	/**
	 * 특정 기간의 활성 사용자 수 (방문자)
	 */
	int countActiveUsersByPeriod(LocalDate startDate, LocalDate endDate);
}
