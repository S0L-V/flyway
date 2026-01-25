package com.flyway.pricing.batch.mariadb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Profile("dev") // dev 프로파일일 때만 빈 등록
@RequiredArgsConstructor
public class ManualJobRunner implements ApplicationListener<ContextRefreshedEvent> {

    private final JobLauncher jobLauncher;
    private final Job dynamicPriceRepriceJob;

    // 컨텍스트가 갱신될 때마다 이벤트가 발생할 수 있으므로, 딱 한 번만 실행되도록 플래그 설정
    private final AtomicBoolean executed = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 1. Root Context인지 확인
        if (event.getApplicationContext().getParent() != null) {
            return;
        }

        // 2. 이미 실행했다면 스킵
        if (executed.getAndSet(true)) {
            return;
        }

        // 배치 전용 스레드를 만들어 비동기로 실행
        new Thread(() -> {
            log.info("============== [MANUAL BATCH START (Async)] ==============");
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("asOf", System.currentTimeMillis())
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters();

                jobLauncher.run(dynamicPriceRepriceJob, jobParameters);

                log.info("============== [MANUAL BATCH END] ==============");
            } catch (Exception e) {
                log.error("배치 수동 실행 중 오류 발생", e);
            }
        }).start(); // 스레드 즉시 시작
    }
}