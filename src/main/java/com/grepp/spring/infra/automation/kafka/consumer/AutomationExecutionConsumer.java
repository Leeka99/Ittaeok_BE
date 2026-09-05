package com.grepp.spring.infra.automation.kafka.consumer;

import com.grepp.spring.app.model.automation.event.AutomationRequestedEvent;
import com.grepp.spring.app.model.n8n.service.N8nService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationExecutionConsumer {
    private final N8nService n8nService;
    private final Bucket automationRateLimitBucket;
    @KafkaListener(
        topics = "automation-requested-events",
        groupId = "automation-execution-group"
    )
    public void consume(AutomationRequestedEvent event) {

        try {

            automationRateLimitBucket
                .asBlocking()
                .consume(1);

            n8nService.sendScheduleConfirmed(
                event.scheduleId(),
                event.scheduleName(),
                event.startTime(),
                event.endTime()
            );

            log.info(
                "[자동화 실행 요청 처리] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId()
            );

        } catch (Exception e) {

            log.error(
                "[자동화 실행 요청 처리 실패] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId(),
                e
            );

            throw new IllegalStateException(e);
        }
    }
}
