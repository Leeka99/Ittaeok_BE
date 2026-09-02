package com.grepp.spring.app.model.automation.service;

import com.grepp.spring.app.model.n8n.service.N8nService;
import io.github.bucket4j.Bucket;
import java.time.LocalDateTime;
import java.util.concurrent.RejectedExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationTaskService {

    @Qualifier("automationExecutor")
    private final ThreadPoolTaskExecutor automationExecutor;

    @Qualifier("automationRateLimitBucket")
    private final Bucket automationRateLimitBucket;

    private final N8nService n8nService;

    public void submit(
        Long scheduleId,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        try { // Queue에 넣는 것 자체가 실패 - queue 초과
            automationExecutor.execute(() -> {
                try { // Queue에는 들어갔지만 Worker가 처리하다 실패

                    automationRateLimitBucket.asBlocking().consume(1);

                    log.info("[자동화 시작] thread={}, scheduleId={}",
                        Thread.currentThread().getName(), scheduleId);

                    n8nService.sendScheduleConfirmed(
                        scheduleId, scheduleName, startTime, endTime);

                    log.info("[자동화 완료] scheduleId={}", scheduleId);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    log.warn(
                        "[자동화 Rate Limit 대기 중 인터럽트] scheduleId={}",
                        scheduleId
                    );
                } catch (Exception e) {

                    log.error("[자동화 실패] scheduleId={}, error={}",
                        scheduleId, e.getMessage());

                    throw e;
                }
            });
        } catch (RejectedExecutionException e) {

            log.error(
                "[자동화 Queue 초과] scheduleId={}, queueSize={}",
                scheduleId,
                automationExecutor.getThreadPoolExecutor()
                    .getQueue()
                    .size()
            );
            throw e;
        }

    }
}
