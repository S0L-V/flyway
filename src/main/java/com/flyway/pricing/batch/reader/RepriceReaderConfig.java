package com.flyway.pricing.batch.reader;

import com.flyway.pricing.batch.row.RepriceCandidateRow;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reader 객체(Bean)를 생성하고 SQL을 정의하는 설정 파일
 */
@Configuration
public class RepriceReaderConfig {

    private static final int CHUNK_SIZE = 500;   // page size와 동일
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Bean
    @StepScope
    public JdbcPagingItemReader<RepriceCandidateRow> repriceCandidateReader(
            DataSource dataSource,
            PagingQueryProvider repriceQueryProvider,
            @Value("#{jobParameters['asOf']}") Long asOfEpochMillis
    ) {
        if (asOfEpochMillis == null) {
            throw new IllegalArgumentException("JobParameter 'asOf' (epoch millis) is required.");
        }

        // 1. 기준 시간 변환 (KST)
        LocalDateTime referenceTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(asOfEpochMillis), KST
        );

        // 2. 파라미터 맵 (LocalDateTime을 그대로 넘기면 드라이버가 알아서 처리)
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("asOf", referenceTime);

        return new JdbcPagingItemReaderBuilder<RepriceCandidateRow>()
                .name("repriceCandidateReader")
                .dataSource(dataSource)
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .queryProvider(repriceQueryProvider) // MariaDB 호환
                .parameterValues(parameterValues)
                // DTO 필드명과 컬럼 별칭(Alias)이 일치하면 자동 매핑
                .rowMapper(new BeanPropertyRowMapper<>(RepriceCandidateRow.class))
                .build();
    }

    /**
     * MariaDB/MySQL 계열에서 사용 가능한 PagingQueryProvider
     * - 정렬키는 "유일하게" 만들 것: (departure_time, flight_id, cabin_class_code) 조합 추천
     */
    @Bean
    public PagingQueryProvider repriceQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();

        // 2. 필수 설정
        factory.setDataSource(dataSource);
        factory.setDatabaseType("MySQL"); // H2를 MySQL 모드로 쓰므로 명시적으로 지정

        // 3. 쿼리 설정
        // (1) SELECT 절
        factory.setSelectClause(
                "SELECT temp.flight_id AS flight_id, " +
                        "       temp.departure_time AS departure_time, " +
                        "       temp.cabin_class_code AS cabin_class_code, " +
                        "       temp.current_price AS current_price, " +
                        "       temp.base_price AS base_price, " +
                        "       temp.last_event_priced_at AS last_event_priced_at"
        );

        // (2) FROM 절 : 서브쿼리를 사용하여 JOIN과 컬럼명을 미리 정리
        factory.setFromClause(
                "FROM (" +
                        "  SELECT f.flight_id, " +
                        "         f.departure_time, " +
                        "         fsp.cabin_class_code, " +
                        "         fsp.current_price, " +
                        "         fsp.base_price, " +
                        "         fsp.last_event_priced_at " +
                        "  FROM flight f " +
                        "  JOIN flight_seat_price fsp ON f.flight_id = fsp.flight_id " +
                        ") AS temp"
        );
        // (3) WHERE 절 : 서브쿼리 별칭 'temp'를 사용하여 조건 걸기
        factory.setWhereClause(
                "WHERE departure_time < TIMESTAMPADD(DAY, 30, CAST(:asOf AS DATETIME))"
        );

        // 4. 정렬 키 설정
        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("departure_time", Order.ASCENDING);
        sortKeys.put("flight_id", Order.ASCENDING);
        sortKeys.put("cabin_class_code", Order.ASCENDING);
        factory.setSortKeys(sortKeys);

        // 5. 객체 반환 (FactoryBean이므로 .getObject() 호출 가능)
        return factory.getObject();
    }

}
