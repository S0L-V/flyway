package com.flyway.pricing.batch.h2;

import com.flyway.pricing.service.DynamicPricingCalculator;
import com.flyway.pricing.batch.reader.RepriceReaderConfig;
import com.flyway.pricing.batch.writer.RepriceWriterConfig;
import com.flyway.pricing.batch.processor.RepriceProcessor;
import com.flyway.pricing.batch.step.RepriceStepConfig;
import com.flyway.pricing.policy.PricingPolicyV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *     [H2 테스트 진행을 위한 설정]
 *  1) `RepriceReaderConfig`의 #72 line 주석 처리
 *  2) #110~112의 테이블 alias 제거 (`f.`, `fsp.`)
 *  3) ReprcieStepIntegrationTest.java 실행 후, 원상 복구 *****
 */


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        BatchTestConfig.class,        // 테스트 설정 (H2, UUID) : JobLauncherTestUtils 빈 생성
        RepriceStepConfig.class,      // Step
        RepriceReaderConfig.class,    // Reader
        RepriceWriterConfig.class,    // Writer
        RepriceProcessor.class,       // Processor
        DynamicPricingCalculator.class, // Calculator (실제 로직 사용)
        PricingPolicyV1.class,         // Policy
})
class RepriceStepIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        // 1. 비즈니스 테이블 생성 (H2 메모리 DB)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS aircraft (" +
                "aircraft_id VARCHAR(36) PRIMARY KEY, " +
                "economy_class_seats INT, " +
                "business_class_seats INT, " +
                "first_class_seats INT)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS flight_info (" +
                "flight_id VARCHAR(36) PRIMARY KEY, " +
                "aircraft_id VARCHAR(36), " +
                "economy_class_seat INT, " +
                "business_class_seat INT, " +
                "first_class_seat INT)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS flight (" +
                "flight_id VARCHAR(36) PRIMARY KEY, departure_time DATETIME)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS flight_seat_price (" +
                "flight_id VARCHAR(36), cabin_class_code VARCHAR(10), " +
                "current_price BIGINT, base_price BIGINT, " +
                "last_event_priced_at DATETIME, last_priced_at DATETIME, " +
                "PRIMARY KEY(flight_id, cabin_class_code))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS price_history (" +
                "price_history_id VARCHAR(36), " +
                "flight_id VARCHAR(36), " +
                "cabin_class_code VARCHAR(10), " +
                "current_price BIGINT, " +
                "old_price BIGINT, " +
                "update_type VARCHAR(20), " +
                "policy_version VARCHAR(20), " +
                "trigger_ref_id VARCHAR(36), " +
                "calculated_at DATETIME, " +
                "calc_context_json TEXT)");

        // 2. 테스트 데이터 Insert (출발 5일 남은 항공편)

        // (1) 항공기 정보 (총 100석)
        jdbcTemplate.update("INSERT INTO aircraft VALUES ('AC-001', 100, 30, 10)");

        // (2) 운항 정보 (FK 연결, 잔여석 50석 설정)
        jdbcTemplate.update("INSERT INTO flight_info VALUES ('F1', 'AC-001', 50, 15, 5)");

        // (3) 항공편 및 가격 정보
        jdbcTemplate.update("INSERT INTO flight VALUES ('F1', DATEADD('DAY', 5, NOW()))");
        jdbcTemplate.update("INSERT INTO flight_seat_price VALUES ('F1', 'ECO', 100000, 100000, NULL, NOW())");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DROP TABLE flight_seat_price");
        jdbcTemplate.execute("DROP TABLE price_history");
        jdbcTemplate.execute("DROP TABLE flight");
        jdbcTemplate.execute("DROP TABLE flight_info");
        jdbcTemplate.execute("DROP TABLE aircraft");
    }

    @Test
    @DisplayName("Step 실행 테스트")
    void testStepExecution() {

        // Reader가 요구하는 필수 파라미터(rangeStart, rangeEnd) 추가
        String rangeStart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String rangeEnd = LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("asOf", System.currentTimeMillis())       // Processor
                .addString("rangeStart", rangeStart)               // Reader
                .addString("rangeEnd", rangeEnd)                   // Reader
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        // when
        // 배치 Step 실행
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("repriceFlightsStep", jobParameters);

        // then
        // 1. 배치 상태 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 2. [Writer 검증 1] 가격 테이블 업데이트 확인
        Long updatedPrice = jdbcTemplate.queryForObject(
                "SELECT current_price FROM flight_seat_price WHERE flight_id = 'F1'", Long.class);

        System.out.println("Updated Price: " + updatedPrice);
        assertThat(updatedPrice).isGreaterThan(100000L);

        // 3. [Writer 검증 2] 히스토리 테이블 Insert 확인
        Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM price_history WHERE flight_id = 'F1'", Integer.class);

        assertThat(historyCount).isEqualTo(1);
    }
}
