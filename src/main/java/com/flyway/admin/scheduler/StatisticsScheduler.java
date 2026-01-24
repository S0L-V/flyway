package com.flyway.admin.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.flyway.admin.dto.StatisticsDto;
import com.flyway.admin.mapper.StatisticsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

	private final StatisticsMapper statisticsMapper;

	/**
	 * 매일 자정 00:05 - 전일 일일 통계 계산
	 */
	@Scheduled(cron = "0 5 0 * * *")
	public void calculateDailyStatistics() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		log.info("[Statistics] 일일 통계 계산 시작: {}", yesterday);

		try {
			StatisticsDto stats = calculateStatistics("DAILY", yesterday, yesterday);
			statisticsMapper.upsertStatistics(stats);
			log.info("[Statistics] 일일 통계 저장 완료: date={}, reservations={}, revenue={}",
				yesterday, stats.getTotalReservations(), stats.getTotalRevenue());
		} catch (Exception e) {
			log.error("[Statistics] 일일 통계 계산 실패: {}", e.getMessage(), e);
		}
	}

	/**
	 * 매주 월요일 00:10 - 전주 주간 통계 계산
	 */
	@Scheduled(cron = "0 10 0 * * MON")
	public void calculateWeeklyStatistics() {
		// 전주 월요일 ~ 일요일
		LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate lastSunday = lastMonday.plusDays(6);

		log.info("[Statistics] 주간 통계 계산 시작: {} ~ {}", lastMonday, lastSunday);

		try {
			StatisticsDto stats = calculateStatistics("WEEKLY", lastMonday, lastSunday);
			// 주간 통계는 해당 주의 월요일 날짜로 지정
			stats.setStatDate(lastMonday);
			statisticsMapper.upsertStatistics(stats);
			log.info("[Statistics] 주간 통계 저장 완료: week={}, reservations={}, revenue={}",
				lastMonday, stats.getTotalReservations(), stats.getTotalRevenue());
		} catch (Exception e) {
			log.error("[Statistics] 주간 통계 계산 실패 {}", e.getMessage(), e);
		}
	}

	/**
	 * 매일 1월 00:15 - 전월 월간 통계 계산
	 */
	@Scheduled(cron = "0 15 0 1 * *")
	public void calculateMonthlyStatistics() {
		// 전월 1일 ~ 말일
		LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
		LocalDate lastDayOfLastMonth = firstDayOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());

		log.info("[Statistics] 월간 통계 계산 시작: {} ~ {}", firstDayOfLastMonth, lastDayOfLastMonth);

		try {
			StatisticsDto stats = calculateStatistics("MONTHLY", firstDayOfLastMonth, lastDayOfLastMonth);
			// 월간 통계는 해당 월의 1일 날짜로 저장
			stats.setStatDate(firstDayOfLastMonth);
			statisticsMapper.upsertStatistics(stats);
			log.info("[Statistics] 월간 통계 저장 완료: month={}, reservations={}, revenue={}",
				firstDayOfLastMonth, stats.getTotalReservations(), stats.getTotalRevenue());
		} catch (Exception e) {
			log.error("[Statistics] 월간 통계 계산 실패: {}", e.getMessage(), e);
		}
	}

	/**
	 * 통계 계산 공통 메서드
	 */
	private StatisticsDto calculateStatistics(String statType, LocalDate startDate, LocalDate endDate) {
		int totalReservations = statisticsMapper.countReservationsByPeriod(startDate, endDate);
		int confirmedReservations = statisticsMapper.countConfirmedReservationsByPeriod(startDate, endDate);
		int cancelledReservation = statisticsMapper.countCancelledReservationsByPeriod(startDate, endDate);
		long totalRevenue = statisticsMapper.sumRevenueByPeriod(startDate, endDate);
		long avgTicketPrice = statisticsMapper.avgTicketPriceByPeriod(startDate, endDate);
		int refundCount = statisticsMapper.countRefundsByPeriod(startDate, endDate);
		long totalRefunds = statisticsMapper.sumRefundsByPeriod(startDate, endDate);
		int newUsers = statisticsMapper.countNewUsersByPeriod(startDate, endDate);
		int activeUsers = statisticsMapper.countActiveUsersByPeriod(startDate, endDate);

		return StatisticsDto.builder()
			.statId(UUID.randomUUID().toString())
			.statType(statType)
			.statDate(startDate)
			.totalReservations(totalReservations)
			.confirmedReservations(confirmedReservations)
			.cancelledReservations(cancelledReservation)
			.totalRevenue(totalRevenue)
			.averageTicketPrice(avgTicketPrice)
			.refundCount(refundCount)
			.totalRefunds(totalRefunds)
			.newUsers(newUsers)
			.activeUsers(activeUsers)
			.build();
	}
}
