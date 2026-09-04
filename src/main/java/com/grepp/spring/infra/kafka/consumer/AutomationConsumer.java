package com.grepp.spring.infra.kafka.consumer;

import com.grepp.spring.app.model.schedule.event.ScheduleFixedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AutomationConsumer {
    @KafkaListener(
        topics = "schedule-fixed-events",
        groupId = "automation-group"
    )
    public void consume(ScheduleFixedEvent event) {

        log.info(
            "[Kafka 자동화 이벤트 수신] eventId={}, scheduleId={}, scheduleName={}",
            event.eventId(),
            event.scheduleId(),
            event.scheduleName()
        );
    }
}
