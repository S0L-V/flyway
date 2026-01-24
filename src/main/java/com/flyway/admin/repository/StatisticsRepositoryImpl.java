package com.flyway.admin.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.StatisticsDto;
import com.flyway.admin.mapper.StatisticsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

	private final StatisticsMapper statisticsMapper;

	@Override
	public int saveStatistics(StatisticsDto statistics) {
		return statisticsMapper.upsertStatistics(statistics);
	}

	@Override
	public StatisticsDto findStatistics(String statType, LocalDate statDate) {
		return statisticsMapper.selectStatistics(statType, statDate);
	}

	@Override
	public List<StatisticsDto> findStatisticsByPeriod(String statType, LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.selectStatisticsByPeriod(statType, startDate, endDate);
	}

	@Override
	public List<StatisticsDto> findRecentDailyStatistics(int days) {
		return statisticsMapper.selectRecentDailyStatistics(days);
	}

	@Override
	public int countReservationsByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countReservationsByPeriod(startDate, endDate);
	}

	@Override
	public int countConfirmedReservationsByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countConfirmedReservationsByPeriod(startDate, endDate);
	}

	@Override
	public int countCancelledReservationsByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countCancelledReservationsByPeriod(startDate, endDate);
	}

	@Override
	public long sumRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.sumRevenueByPeriod(startDate, endDate);
	}

	@Override
	public long avgTicketPriceByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.avgTicketPriceByPeriod(startDate, endDate);
	}

	@Override
	public int countRefundsByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countRefundsByPeriod(startDate, endDate);
	}

	@Override
	public long sumRefundsByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.sumRefundsByPeriod(startDate, endDate);
	}

	@Override
	public int countNewUsersByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countNewUsersByPeriod(startDate, endDate);
	}

	@Override
	public int countActiveUsersByPeriod(LocalDate startDate, LocalDate endDate) {
		return statisticsMapper.countActiveUsersByPeriod(startDate, endDate);
	}
}
