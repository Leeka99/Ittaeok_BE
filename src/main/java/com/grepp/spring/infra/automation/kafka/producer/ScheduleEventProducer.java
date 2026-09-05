package com.grepp.spring.infra.automation.kafka.producer;

import com.grepp.spring.app.model.automation.event.ScheduleConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventProducer {

    private static final String TOPIC = "schedule-confirmed-events";

    private final KafkaTemplate<String, ScheduleConfirmedEvent> kafkaTemplate;

    public void publishScheduleFixed(ScheduleConfirmedEvent event) {

        kafkaTemplate.send(
            TOPIC,
            event.scheduleId().toString(),
            event
        );

        log.info(
            "[Kafka 일정 확정 이벤트 발행] eventId={}, scheduleId={}",
            event.eventId(),
            event.scheduleId()
        );
    }
}