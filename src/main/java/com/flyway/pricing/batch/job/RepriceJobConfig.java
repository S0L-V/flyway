package com.flyway.pricing.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Batch Job의 Bean을 정의
 */
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RepriceJobConfig extends DefaultBatchConfigurer {

    public static final String JOB_NAME = "repriceJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    // 스프링 컨테이너의 트랜잭션 매니저를 강제로 사용하게 함
    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    @PostConstruct // 빈 생성 직후 실행되어 부모 설정에 DataSource를 주입
    public void initialize() {
        try {
            super.setDataSource(this.dataSource);
            super.initialize(); //부모의 초기화 로직 실행 (JobRepository, JobLauncher 생성)

        } catch (Exception e) {
            throw new RuntimeException("Batch Configurer 초기화 실패", e);
        }
    }

    @Bean
    public Job repriceJob(Step repriceFlightsStep) {
        return jobBuilderFactory.get(JOB_NAME)
                .validator(asOfRequiredValidator())
                .start(repriceFlightsStep)
                .build();
    }

    /**
     * JobParameter "asOf"(epoch millis) 없으면 실행 실패시키는 검증기
     * - Reader/Processor/Writer가 동일 기준 시각을 쓰도록 강제
     */
    @Bean
    public JobParametersValidator asOfRequiredValidator() {
        return parameters -> {
            if (parameters == null || parameters.getLong("asOf") == null) {
                throw new JobParametersInvalidException("JobParameter 'asOf' (epoch millis) is required.");
            }
        };
    }
}