package com.flyway.pricing.batch.reader;

import com.flyway.pricing.batch.row.RepriceCandidateRow;
import lombok.RequiredArgsConstructor;
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
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reader 객체(Bean)를 생성하고 SQL을 정의하는 설정 파일
 */
@Configuration
@RequiredArgsConstructor
public class RepriceReaderConfig {

    private final DataSource dataSource;
    private static final int CHUNK_SIZE = 500;   // page size와 동일
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * JobParameter를 받아 실제 값(Map)을 세팅하는 역할
     * 아래의 repriceQueryProvider를 주입받아 SQL을 실행함
     */
    @Bean
    @StepScope
    public JdbcPagingItemReader<RepriceCandidateRow> repriceItemReader(
            PagingQueryProvider repriceQueryProvider,
//            @Value("#{jobParameters['asOf']}") Long asOfEpochMillis,
            @Value("#{jobParameters['rangeStart']}") String rangeStart, // "yyyy-MM-dd HH:mm:ss"
            @Value("#{jobParameters['rangeEnd']}") String rangeEnd
    ) {

        if (rangeStart == null || rangeEnd == null) {
            throw new IllegalArgumentException("Job Parameters 'rangeStart', 'rangeEnd' 가 누락되었습니다.");
        }

        // 2. 쿼리에 바인딩할 파라미터 Map 생성
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("rangeStart", rangeStart);
        parameterValues.put("rangeEnd", rangeEnd);

        return new JdbcPagingItemReaderBuilder<RepriceCandidateRow>()
                .name("repriceItemReader")
                .dataSource(this.dataSource)
                .queryProvider(repriceQueryProvider)
                .parameterValues(parameterValues)
                .pageSize(CHUNK_SIZE)
                .rowMapper(new BeanPropertyRowMapper<>(RepriceCandidateRow.class))
                .build();
    }

    /**
     * PagingReader가 사용할 SQL 구조를 정의하는 역할
     */
    @Bean
    public PagingQueryProvider repriceQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(this.dataSource);
        // factory.setDatabaseType("MySQL");

        // 1) SELECT : CASE문으로 좌석 정보 계산
        factory.setSelectClause(
                "SELECT f.flight_id, " +
                        "       f.departure_time, " +
                        "       fsp.cabin_class_code, " +
                        "       fsp.current_price, " +
                        "       fsp.base_price, " +
                        "       fsp.last_event_priced_at, " +
                        "       CASE " +
                        "           WHEN fsp.cabin_class_code = 'ECO' THEN fi.economy_class_seat " +
                        "           WHEN fsp.cabin_class_code = 'BIZ' THEN fi.business_class_seat " +
                        "           WHEN fsp.cabin_class_code = 'FST' THEN fi.first_class_seat " +
                        "       END AS remaining_seats, " +
                        "       CASE " +
                        "           WHEN fsp.cabin_class_code = 'ECO' THEN ac.economy_class_seats " +
                        "           WHEN fsp.cabin_class_code = 'BIZ' THEN ac.business_class_seats " +
                        "           WHEN fsp.cabin_class_code = 'FST' THEN ac.first_class_seats " +
                        "       END AS total_seats "
        );

        // 2) FROM
        factory.setFromClause(
                "FROM flight f " +
                        "JOIN flight_seat_price fsp ON f.flight_id = fsp.flight_id " +
                        "JOIN flight_info fi ON f.flight_id = fi.flight_id " +
                        "JOIN aircraft ac ON fi.aircraft_id = ac.aircraft_id "
        );

        // 3) WHERE : 스케줄러가 정해준 시간 범위로 필터링
        factory.setWhereClause(
                "WHERE f.departure_time >= CAST(:rangeStart AS DATETIME) " +
                        "  AND f.departure_time < CAST(:rangeEnd AS DATETIME)"
        );

        // 4) 정렬
        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("f.departure_time", Order.ASCENDING);
        sortKeys.put("f.flight_id", Order.ASCENDING);
        sortKeys.put("fsp.cabin_class_code", Order.ASCENDING);
        factory.setSortKeys(sortKeys);

        return factory.getObject();
    }
}
