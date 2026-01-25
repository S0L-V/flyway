package com.flyway.pricing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SeatAvailabilityServiceTest {

    @Mock
    DataSource dataSource; // 생성자 주입용
    @Mock JdbcTemplate jdbcTemplate; // 실제 동작 제어용

    SeatAvailabilityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeatAvailabilityServiceImpl(dataSource);

        // 내부 jdbcTemplate을 mock으로 교체
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
    }

    @Test
    @DisplayName("정상 조회: ECO 등급의 좌석 정보를 올바르게 파싱")
    void getSeatStatus_success_ECO() {
        // given
        when(jdbcTemplate.queryForMap(anyString(), any()))
                .thenReturn(Map.of(
                        "rem_eco", 80, "tot_eco", 180,
                        "rem_biz", 10, "tot_biz", 30,
                        "rem_fst", 2,  "tot_fst", 8
                ));

        // when
        SeatAvailabilityServiceImpl.SeatStatus status =
                service.getSeatStatus("FLIGHT-001", "ECO");

        // then
        assertThat(status.getTotalSeats()).isEqualTo(180);
        assertThat(status.getRemainingSeats()).isEqualTo(80);
    }

    @Test
    @DisplayName("DB 조회 중 예외가 발생하면 (0,0) 리턴")
    void getSeatStatus_dbError_returnsZeroZero() {
        // given
        when(jdbcTemplate.queryForMap(anyString(), any()))
                .thenThrow(new RuntimeException("SQL Error"));

        // when
        SeatAvailabilityServiceImpl.SeatStatus status =
                service.getSeatStatus("FLIGHT-001", "ECO");

        // then
        assertThat(status.getTotalSeats()).isZero();
        assertThat(status.getRemainingSeats()).isZero();
    }
}