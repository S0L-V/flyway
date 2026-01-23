package com.flyway.admin.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.dto.StatisticsDto;

@Mapper
public interface StatisticsMapper {

	/**
	 * 통계 저장 (INSERT OR UPDATE)
	 */
	int upsertStatistics(StatisticsDto statistics);

	/**
	 * 특정 날짜/유형의 통계 조회
	 */
	StatisticsDto selectStatistics(
		@Param("statType") String statType,
		@Param("statDate")LocalDateTime statDate
	);

	/**
	 * 기간별 통계 목록 조회
	 */
	List<StatisticsDto> selectStatisticsByPeriod(
		@Param("statType") String statType,
		@Param("StartDate")LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 최근 N일간 일일 통걔 조회
	 */
	List<StatisticsDto> selectRecentDailyStatistics(@Param("days") int days);

	// =======================================
	// 통계 계산용 쿼리 (원본 테이블에서 집계)
	// =======================================

	/**
	 * 특정 기간의 총 예약 건수
	 */
	int countReservationsByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 확정 예약 건수 (결제 완료)
	 */
	int countConfirmedReservationsByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 총 매출
	 */
	long sumRevenueByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 평균 티켓 가격
	 */
	long avgTicketPriceByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 환불 건수
	 */
	int countRefundsByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 신규 가입 회원 수
	 */
	int countNewUsersByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 기간의 활성 사용자 수 (방문자)
	 */
	int countActiveUsersByPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);
}
