package com.grepp.spring.infra.automation.kafka.consumer;

import com.grepp.spring.app.model.automation.code.AutomationTrigger;
import com.grepp.spring.app.model.automation.event.AutomationRequestedEvent;
import com.grepp.spring.app.model.automation.event.ScheduleConfirmedEvent;
import com.grepp.spring.app.model.n8n.service.N8nService;
import com.grepp.spring.infra.automation.kafka.producer.AutomationEventProducer;
import io.github.bucket4j.Bucket;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationRequestConsumer {

    private final AutomationEventProducer automationEventProducer;

    @KafkaListener(
        topics = "schedule-confirmed-events",
        groupId = "automation-group"
    )
    public void consume(ScheduleConfirmedEvent event) {

        AutomationRequestedEvent requestedEvent =
            new AutomationRequestedEvent(
                UUID.randomUUID().toString(),
                event.scheduleId(),
                event.scheduleName(),
                event.startTime(),
                event.endTime(),
                AutomationTrigger.SCHEDULE_CONFIRMED,
                LocalDateTime.now()
            );

        automationEventProducer.publishRequested(requestedEvent);

        log.info(
            "[자동화 실행 요청 생성] scheduleId={}",
            event.scheduleId()
        );
    }
}