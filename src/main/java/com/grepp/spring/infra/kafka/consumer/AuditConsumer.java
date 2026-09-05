package com.grepp.spring.infra.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grepp.spring.app.model.schedule.entity.EventHistory;
import com.grepp.spring.app.model.schedule.event.ScheduleFixedEvent;
import com.grepp.spring.app.model.schedule.repository.EventHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {
    private final EventHistoryRepository eventHistoryRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "schedule-fixed-events",
        groupId = "audit-group"
    )
    public void consume(ScheduleFixedEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            EventHistory history = new EventHistory(
                event.eventId(),
                "SCHEDULE_FIXED",
                event.scheduleId(),
                event.occurredAt(),
                payload
            );

            eventHistoryRepository.save(history);

            log.info(
                "[Kafka 일정 확정 이력 저장] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId()
            );

        } catch (JsonProcessingException e) {
            log.error(
                "[Kafka Audit payload 변환 실패] eventId={}",
                event.eventId(),
                e
            );

            throw new IllegalStateException(e);
        }
    }
}