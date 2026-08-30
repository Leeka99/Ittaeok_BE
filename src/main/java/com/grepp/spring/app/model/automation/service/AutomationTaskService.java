package com.grepp.spring.app.model.automation.service;

import com.grepp.spring.app.model.n8n.service.N8nService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AutomationTaskService {

    private final ThreadPoolTaskExecutor automationExecutor;
    private final N8nService n8nService;

    public AutomationTaskService(
        @Qualifier("automationExecutor")
        ThreadPoolTaskExecutor automationExecutor,
        N8nService n8nService
    ) {
        this.automationExecutor = automationExecutor;
        this.n8nService = n8nService;
    }

    public void submit(
        Long scheduleId,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        automationExecutor.execute(() -> {
            try {
                log.info("[자동화 시작] thread={}, scheduleId={}",
                    Thread.currentThread().getName(), scheduleId);

                n8nService.sendScheduleConfirmed(
                    scheduleId, scheduleName, startTime, endTime);

                log.info("[자동화 완료] scheduleId={}", scheduleId);

            } catch (Exception e) {

                log.error("[자동화 실패] scheduleId={}, error={}",
                    scheduleId, e.getMessage());

                throw e;
            }
        });
    }
}
