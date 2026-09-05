package com.grepp.spring.infra.automation;

import io.github.bucket4j.Bucket;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutomationRateLimitConfig {

    @Bean(name = "automationRateLimitBucket")
    public Bucket automationRateLimitBucket() {

        return Bucket.builder()
            .addLimit(limit -> limit
                .capacity(1)
                .refillGreedy(
                    1,
                    Duration.ofMillis(400)
                )
            )
            .build();
    }
}
