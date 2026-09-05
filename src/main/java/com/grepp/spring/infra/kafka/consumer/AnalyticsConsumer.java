package com.grepp.spring.infra.kafka.consumer;

import com.grepp.spring.app.model.schedule.event.ScheduleFixedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalyticsConsumer {
    @KafkaListener(
        topics = "schedule-fixed-events",
        groupId = "analytics-group"
    )
    public void consume(ScheduleFixedEvent event) {

        log.info(
            "[Replay 검증] eventId={}, scheduleId={}, scheduleName={}",
            event.eventId(),
            event.scheduleId(),
            event.scheduleName()
        );
    }
}
