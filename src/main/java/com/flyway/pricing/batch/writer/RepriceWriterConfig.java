package com.flyway.pricing.batch.writer;

import com.flyway.pricing.batch.row.PricingResultRow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RepriceWriterConfig {

    /**
     * Step에서 호출하는 최종 Writer
     * Update와 (필터링된) Insert를 순차적으로 실행한다.
     */
    @Bean
    public CompositeItemWriter<PricingResultRow> repriceCompositeWriter(
            ItemWriter<PricingResultRow> flightSeatPriceUpdateWriter,
            ItemWriter<PricingResultRow> filteringHistoryWriter
    ) {
        return new CompositeItemWriterBuilder<PricingResultRow>()
                .delegates(flightSeatPriceUpdateWriter, filteringHistoryWriter)
                .build();
    }

    /**
     * [Writer 1] 무조건 실행: 현재 판매가(current_price) UPDATE
     * - last_priced_at을 기준 시각(calculatedAt())으로 갱신
     * - (중요) 동시성 방어: 누군가 그새 가격을 바꿨다면(current_price != old) 업데이트 실패하도록 처리
     */
    @Bean
    public JdbcBatchItemWriter<PricingResultRow> flightSeatPriceUpdateWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PricingResultRow>()
                .dataSource(dataSource)
                .sql("UPDATE flight_seat_price " +
                        "SET current_price = :newPrice, " +
                        "    last_priced_at = :calculatedAt " +
                        "WHERE flight_id = :flightId " +
                        "  AND cabin_class_code = :cabinClassCode " +
                        // 낙관적 락(Optimistic Lock): 내가 읽은 가격과 다르면(누가 수정했으면) 업데이트 안 함 (0 row updated)
                        "  AND current_price = :currentPrice")
                .beanMapped()
                .assertUpdates(false) // 낙관적 락에 의해 업데이트가 0건이어도 에러 내지 않음
                .build();
    }

    /**
     * [Writer 2-A] 필터링 로직 (거름망)
     * - 500원 이상 차이가 나는 항목만 골라내서 실제 Insert Writer에게 전달한다.
     */
    @Bean
    public ItemWriter<PricingResultRow> filteringHistoryWriter(
            JdbcBatchItemWriter<PricingResultRow> priceHistoryDelegateWriter
    ) {
        return items -> {
            // 1. 조건(Diff >= 500)을 만족하는 아이템만 필터링
            List<PricingResultRow> itemsToWrite = items.stream()
                    .filter(item -> Math.abs(item.getNewPrice() - item.getCurrentPrice()) >= 500)
                    .collect(Collectors.toList());

            // 2. 필터링된 리스트가 존재할 때만 실제 DB Insert 수행
            if (!itemsToWrite.isEmpty()) {
                priceHistoryDelegateWriter.write(itemsToWrite);
            }
        };
    }

    /**
     * [Writer 2-B] 실제 Insert 수행 (실행기)
     * - price_history 테이블에 이력을 적재한다.
     * - PK인 price_history_id는 MariaDB의 UUID() 함수를 사용해 자동 생성한다.
     */
    @Bean
    public JdbcBatchItemWriter<PricingResultRow> priceHistoryDelegateWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PricingResultRow>()
                .dataSource(dataSource)
                .sql("INSERT INTO price_history (" +
                        "  price_history_id, " +
                        "  flight_id, " +
                        "  cabin_class_code, " +
                        "  current_price, " + // 변경 후 가격
                        "  old_price, " + // 변경 전 가격
                        "  update_type, " +
                        "  policy_version, " +
                        "  trigger_ref_id, " +
                        "  calc_context_json, " +
                        "  calculated_at " +
                        ") VALUES (" +
                        "  UUID(), " +            // MariaDB UUID 함수
                        "  :flightId, " +
                        "  :cabinClassCode, " +
                        "  :newPrice, " +
                        "  :currentPrice, " +
                        "  'BATCH', " +
                        "  :policyVersion, " +
                        "  NULL, " +             // 배치는 이벤트 트리거 ID 없음
                        "  :calcContextJson, " +
                        "  :calculatedAt)")
                .beanMapped()
                .build();
    }
}