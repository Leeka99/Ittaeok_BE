package com.grepp.spring.infra.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AutomationExecutorConfig {

    @Bean(name = "automationExecutor")
    public ThreadPoolTaskExecutor automationExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 동시에 처리하는 Worker 수
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);

        // Worker가 처리하지 못한 작업이 기다리는 Queue 크기
        executor.setQueueCapacity(100);

        // 로그에서 Worker 확인용
        executor.setThreadNamePrefix("ittaeOk-automation-worker-");

        // Worker 1개 + Queue 100개까지 모두 찬 경우 예외 발생
        executor.setRejectedExecutionHandler(
            new ThreadPoolExecutor.AbortPolicy()
        );

        // 정상 종료 시 진행 중인 작업을 기다림
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        return executor;
    }
}
