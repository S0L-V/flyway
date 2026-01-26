package com.flyway.admin.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.flyway.admin.dto.StatisticsDto;
import com.flyway.admin.repository.StatisticsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 통계 스케줄러
 * - 매일 자정: 전일 일일 통계 계산
 * - 매주 월요일 자정: 전주 주간 통계 계산
 * - 매월 1일 자정: 전월 월간 통계 계산
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

	private final StatisticsRepository statisticsRepository;

	/**
	 * 매일 자정 00:05 - 전일 일일 통계 계산
	 */
	@Scheduled(cron = "0 5 0 * * *")
	public void calculateDailyStatistics() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		log.info("[Statistics] 일일 통계 계산 시작: {}", yesterday);

		try {
			StatisticsDto stats = calculateStatistics("DAILY", yesterday, yesterday);
			statisticsRepository.saveStatistics(stats);
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
			statisticsRepository.saveStatistics(stats);
			log.info("[Statistics] 주간 통계 저장 완료: week={}, reservations={}, revenue={}",
				lastMonday, stats.getTotalReservations(), stats.getTotalRevenue());
		} catch (Exception e) {
			log.error("[Statistics] 주간 통계 계산 실패 {}", e.getMessage(), e);
		}
	}

	/**
	 * 매월 1일 00:15 - 전월 월간 통계 계산
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
			statisticsRepository.saveStatistics(stats);
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
		int totalReservations = statisticsRepository.countReservationsByPeriod(startDate, endDate);
		int confirmedReservations = statisticsRepository.countConfirmedReservationsByPeriod(startDate, endDate);
		int cancelledReservations = statisticsRepository.countCancelledReservationsByPeriod(startDate, endDate);
		long totalRevenue = statisticsRepository.sumRevenueByPeriod(startDate, endDate);
		long avgTicketPrice = statisticsRepository.avgTicketPriceByPeriod(startDate, endDate);
		int refundCount = statisticsRepository.countRefundsByPeriod(startDate, endDate);
		long totalRefunds = statisticsRepository.sumRefundsByPeriod(startDate, endDate);
		int newUsers = statisticsRepository.countNewUsersByPeriod(startDate, endDate);
		int activeUsers = statisticsRepository.countActiveUsersByPeriod(startDate, endDate);

		return StatisticsDto.builder()
			.statId(UUID.randomUUID().toString())
			.statType(statType)
			.statDate(startDate)
			.totalReservations(totalReservations)
			.confirmedReservations(confirmedReservations)
			.cancelledReservations(cancelledReservations)
			.totalRevenue(totalRevenue)
			.averageTicketPrice(avgTicketPrice)
			.refundCount(refundCount)
			.totalRefunds(totalRefunds)
			.newUsers(newUsers)
			.activeUsers(activeUsers)
			.build();
	}
}
