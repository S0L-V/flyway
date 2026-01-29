package com.flyway.pricing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Profile("batch")
@Slf4j
@Configuration
@EnableScheduling   // 스케줄링 기능 활성화
@RequiredArgsConstructor
public class RepriceBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job repriceJob;

    // 날짜 포맷
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 1. 장기 예보 배치 (Daily)
     * - 대상: D-30일 ~ D-3일
     * - 빈도: 매일 자정 00:02 실행
     */
    @Scheduled(cron = "0 2 0 * * *")
    public void runLongTermForecast() {
        LocalDateTime now = LocalDateTime.now();

        // 범위 계산: 지금으로부터 2일 뒤 ~ 30일 뒤까지 출발하는 항공편
        String rangeStart = now.plusDays(2).format(FORMATTER);
        String rangeEnd = now.plusDays(30).format(FORMATTER);

        log.info(">>> [LONG_TERM BATCH] STARTED (range: {} ~ {})", rangeStart, rangeEnd);
        runJob("LONG_TERM", rangeStart, rangeEnd);
    }

    /**
     * 2. 단기/임박 예보 배치 (Frequent)
     * - 대상: D-48시간(2일) ~ D-0시간(출발 직전)
     * - 빈도: 1시간마다 실행 (정각)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void runShortTermForecast() {
        LocalDateTime now = LocalDateTime.now();

        // 범위 계산: 지금(출발 임박) ~ 48시간 뒤까지 출발하는 항공편
        String rangeStart = now.format(FORMATTER);
        String rangeEnd = now.plusHours(48).format(FORMATTER);

        log.info(">>> [SHORT_TERM BATCH] STARTED (range: {} ~ {})", rangeStart, rangeEnd);
        runJob("SHORT_TERM", rangeStart, rangeEnd);
    }

    // 공통 실행 로직
    private void runJob(String type, String start, String end) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("type", type)             // 로그/구분용 타입
                    .addString("rangeStart", start)      // 조회 시작일시
                    .addString("rangeEnd", end)          // 조회 종료일시
                    .addLong("asOf", System.currentTimeMillis()) // 기준 시간 (Processor)
                    .addLong("run.id", System.currentTimeMillis()) // 중복 실행 허용을 위한 ID
                    .toJobParameters();

            jobLauncher.run(repriceJob, jobParameters);

        } catch (Exception e) {
            log.error(">>> [BATCH FAILED] type: {}, error: {}", type, e.getMessage(), e);
        }
    }
}