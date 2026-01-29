package com.flyway.batch.runner;

import com.flyway.pricing.batch.BatchRootConfig;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class BatchRunnerApplication {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        // batch 프로파일
        ctx.getEnvironment().setActiveProfiles("batch");

        // xml 로딩 (DataSource, SqlSessionFactory 등)
        new XmlBeanDefinitionReader(ctx).loadBeanDefinitions(
                new ClassPathResource("spring/root-context.xml")
        );

        // Java Config 로딩
        ctx.register(BatchRunnerInfraConfig.class);
        ctx.register(BatchRootConfig.class);

        // 컨텍스트 시작
        ctx.refresh();

        // JVM 유지 (상시 실행)
        Thread.sleep(Long.MAX_VALUE);
    }
}
