package com.flyway.pricing.batch;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("batch")
@ComponentScan(basePackages = {
        "com.flyway.pricing"
})
public class BatchRootConfig {
}
