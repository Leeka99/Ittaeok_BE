package com.grepp.spring.infra.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grepp.spring.app.model.schedule.entity.EventHistory;
import com.grepp.spring.app.model.schedule.event.AutomationDeferredEvent;
import com.grepp.spring.app.model.schedule.repository.EventHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationEventHistoryConsumer {

    private final EventHistoryRepository eventHistoryRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "automation-events",
        groupId = "automation-audit-group"
    )
    public void consume(AutomationDeferredEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            EventHistory eventHistory = new EventHistory(
                event.eventId(),
                "AUTOMATION_DEFERRED",
                event.scheduleId(),
                event.occurredAt(),
                payload
            );

            eventHistoryRepository.save(eventHistory);

            log.info(
                "[AutomationDeferredEvent 이력 저장] eventId={}, scheduleId={}",
                event.eventId(),
                event.scheduleId()
            );

        } catch (JsonProcessingException e) {
            log.error(
                "[AutomationDeferredEvent 직렬화 실패] eventId={}",
                event.eventId(),
                e
            );

            throw new IllegalStateException(e);
        }
    }
}