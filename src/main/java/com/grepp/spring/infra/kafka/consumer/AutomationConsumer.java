package com.grepp.spring.infra.kafka.consumer;

import com.grepp.spring.app.model.n8n.service.N8nService;
import com.grepp.spring.app.model.schedule.event.ScheduleConfirmedEvent;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationConsumer {

    private final N8nService n8nService;
    private final Bucket automationRateLimitBucket;

    @KafkaListener(
        topics = "schedule-confirmed-events",
        groupId = "automation-group"
    )
    public void consume(ScheduleConfirmedEvent event) {

        try {
            automationRateLimitBucket
                .asBlocking()
                .consume(1);

            log.info(
                "[Kafka 자동화 처리 시작] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId()
            );

            n8nService.sendScheduleConfirmed(
                event.scheduleId(),
                event.scheduleName(),
                event.startTime(),
                event.endTime()
            );

            log.info(
                "[Kafka 자동화 처리 완료] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.warn(
                "[Kafka 자동화 Rate Limit 대기 중 인터럽트] scheduleId={}",
                event.scheduleId()
            );

        } catch (Exception e) {

            log.error(
                "[Kafka 자동화 처리 실패] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId(),
                e
            );

            throw e;
        }
    }
}
