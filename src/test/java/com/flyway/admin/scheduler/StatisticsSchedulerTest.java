package com.flyway.admin.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.flyway.admin.dto.StatisticsDto;
import com.flyway.admin.mapper.StatisticsMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("통계 스케줄러 테스트")
class StatisticsSchedulerTest {

	@Mock
	private StatisticsMapper statisticsMapper;

	@InjectMocks
	private StatisticsScheduler scheduler;

	@Captor
	private ArgumentCaptor<StatisticsDto> statsCaptor;

	@BeforeEach
	void setUp() {
		// 공통 Mock 설정
		given(statisticsMapper.countReservationsByPeriod(any(), any())).willReturn(100);
		given(statisticsMapper.countConfirmedReservationsByPeriod(any(), any())).willReturn(80);
		given(statisticsMapper.countCancelledReservationsByPeriod(any(), any())).willReturn(5);
		given(statisticsMapper.sumRevenueByPeriod(any(), any())).willReturn(5000000L);
		given(statisticsMapper.avgTicketPriceByPeriod(any(), any())).willReturn(50000L);
		given(statisticsMapper.countRefundsByPeriod(any(), any())).willReturn(3);
		given(statisticsMapper.sumRefundsByPeriod(any(), any())).willReturn(150000L);
		given(statisticsMapper.countNewUsersByPeriod(any(), any())).willReturn(20);
		given(statisticsMapper.countActiveUsersByPeriod(any(), any())).willReturn(500);
		given(statisticsMapper.upsertStatistics(any())).willReturn(1);
	}

	@Nested
	@DisplayName("일일 통계 계산")
	class DailyStatisticsTest {

		@Test
		@DisplayName("일일 통계 계산 성공")
		void calculateDailyStatistics_Success() {
			// when
			scheduler.calculateDailyStatistics();

			// then
			then(statisticsMapper).should().upsertStatistics(statsCaptor.capture());
			StatisticsDto saved = statsCaptor.getValue();

			assertThat(saved.getStatType()).isEqualTo("DAILY");
			assertThat(saved.getTotalReservations()).isEqualTo(100);
			assertThat(saved.getConfirmedReservations()).isEqualTo(80);
			assertThat(saved.getCancelledReservations()).isEqualTo(5);
			assertThat(saved.getTotalRevenue()).isEqualTo(5000000L);
			assertThat(saved.getAverageTicketPrice()).isEqualTo(50000L);
			assertThat(saved.getRefundCount()).isEqualTo(3);
			assertThat(saved.getTotalRefunds()).isEqualTo(150000L);
			assertThat(saved.getNewUsers()).isEqualTo(20);
			assertThat(saved.getActiveUsers()).isEqualTo(500);
		}

		@Test
		@DisplayName("일일 통계 - 전일 날짜 기준")
		void calculateDailyStatistics_YesterdayDate() {
			// given
			LocalDate yesterday = LocalDate.now().minusDays(1);

			// when
			scheduler.calculateDailyStatistics();

			// then
			then(statisticsMapper).should().countReservationsByPeriod(yesterday, yesterday);
		}

		@Test
		@DisplayName("일일 통계 계산 실패 시 예외 처리")
		void calculateDailyStatistics_Error() {
			// given
			given(statisticsMapper.countReservationsByPeriod(any(), any()))
				.willThrow(new RuntimeException("DB Error"));

			// when & then (예외가 발생해도 스케줄러는 중단되지 않음)
			assertThatCode(() -> scheduler.calculateDailyStatistics())
				.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("주간 통계 계산")
	class WeeklyStatisticsTest {

		@Test
		@DisplayName("주간 통계 계산 성공")
		void calculateWeeklyStatistics_Success() {
			// when
			scheduler.calculateWeeklyStatistics();

			// then
			then(statisticsMapper).should().upsertStatistics(statsCaptor.capture());
			StatisticsDto saved = statsCaptor.getValue();

			assertThat(saved.getStatType()).isEqualTo("WEEKLY");
			assertThat(saved.getTotalReservations()).isEqualTo(100);
		}

		@Test
		@DisplayName("주간 통계 계산 실패 시 예외 처리")
		void calculateWeeklyStatistics_Error() {
			// given
			given(statisticsMapper.countReservationsByPeriod(any(), any()))
				.willThrow(new RuntimeException("DB Error"));

			// when & then
			assertThatCode(() -> scheduler.calculateWeeklyStatistics())
				.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("월간 통계 계산")
	class MonthlyStatisticsTest {

		@Test
		@DisplayName("월간 통계 계산 성공")
		void calculateMonthlyStatistics_Success() {
			// when
			scheduler.calculateMonthlyStatistics();

			// then
			then(statisticsMapper).should().upsertStatistics(statsCaptor.capture());
			StatisticsDto saved = statsCaptor.getValue();

			assertThat(saved.getStatType()).isEqualTo("MONTHLY");
			assertThat(saved.getTotalReservations()).isEqualTo(100);
		}

		@Test
		@DisplayName("월간 통계 계산 실패 시 예외 처리")
		void calculateMonthlyStatistics_Error() {
			// given
			given(statisticsMapper.countReservationsByPeriod(any(), any()))
				.willThrow(new RuntimeException("DB Error"));

			// when & then
			assertThatCode(() -> scheduler.calculateMonthlyStatistics())
				.doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("통계 데이터 검증")
	class StatisticsDataTest {

		@Test
		@DisplayName("statId는 UUID 형식")
		void statId_IsUUID() {
			// when
			scheduler.calculateDailyStatistics();

			// then
			then(statisticsMapper).should().upsertStatistics(statsCaptor.capture());
			String statId = statsCaptor.getValue().getStatId();

			assertThat(statId).matches("[a-f0-9\\-]{36}");
		}

		@Test
		@DisplayName("모든 집계 쿼리가 호출됨")
		void allAggregationQueriesCalled() {
			// when
			scheduler.calculateDailyStatistics();

			// then
			then(statisticsMapper).should().countReservationsByPeriod(any(), any());
			then(statisticsMapper).should().countConfirmedReservationsByPeriod(any(), any());
			then(statisticsMapper).should().countCancelledReservationsByPeriod(any(), any());
			then(statisticsMapper).should().sumRevenueByPeriod(any(), any());
			then(statisticsMapper).should().avgTicketPriceByPeriod(any(), any());
			then(statisticsMapper).should().countRefundsByPeriod(any(), any());
			then(statisticsMapper).should().sumRefundsByPeriod(any(), any());
			then(statisticsMapper).should().countNewUsersByPeriod(any(), any());
			then(statisticsMapper).should().countActiveUsersByPeriod(any(), any());
		}
	}
}