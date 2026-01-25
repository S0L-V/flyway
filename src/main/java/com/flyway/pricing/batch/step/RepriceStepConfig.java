package com.flyway.pricing.batch.step;

import com.flyway.pricing.batch.row.PricingResultRow;
import com.flyway.pricing.batch.row.RepriceCandidateRow;
import com.flyway.pricing.batch.processor.RepriceProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RepriceStepConfig {

    public static final String STEP_NAME = "repriceFlightsStep";
    private static final int CHUNK_SIZE = 500;

    private final StepBuilderFactory stepBuilderFactory;

    private final PlatformTransactionManager transactionManager;

    @Bean
    public Step repriceFlightsStep(
            // 1. Reader 주입
            ItemReader<RepriceCandidateRow> repriceCandidateReader,

            // 2. Processor 주입
            RepriceProcessor repriceProcessor,

            // 3. Writer 주입
            ItemWriter<PricingResultRow> repriceCompositeWriter
    ) {
        return stepBuilderFactory.get(STEP_NAME)
                .<RepriceCandidateRow, PricingResultRow>chunk(CHUNK_SIZE)

                .reader(repriceCandidateReader)
                .processor(repriceProcessor)
                .writer(repriceCompositeWriter)

                .transactionManager(transactionManager) // 트랜잭션 관리자 명시
                .build();
    }
}
