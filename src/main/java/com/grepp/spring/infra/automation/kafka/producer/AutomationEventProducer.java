package com.grepp.spring.infra.automation.kafka.producer;

import com.grepp.spring.app.model.automation.event.AutomationCompletedEvent;
import com.grepp.spring.app.model.automation.event.AutomationDeferredEvent;
import com.grepp.spring.app.model.automation.event.AutomationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationEventProducer {

    private static final String COMPLETE = "automation-completed-events";
    private static final String DEFERRED = "automation-deferred-events";
    private static final String REQUESTED = "automation-requested-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRequested(AutomationRequestedEvent event) {

        kafkaTemplate.send(
            REQUESTED,
            event.scheduleId().toString(),
            event
        );

        log.info(
            "[AutomationRequestedEvent 발행] eventId={}, scheduleId={}",
            event.eventId(),
            event.scheduleId()
        );
    }

    public void publishCompleted(AutomationCompletedEvent event) {
        kafkaTemplate.send(
            COMPLETE,
            event.scheduleId().toString(),
            event
        );

        log.info(
            "[자동화 완료 이벤트 발행] eventId={}, scheduleId={}",
            event.eventId(),
            event.scheduleId()
        );
    }

    public void publishDeferred(AutomationDeferredEvent event) {
        kafkaTemplate.send(
            DEFERRED,
            event.scheduleId().toString(),
            event
        );

        log.info(
            "[자동화 연기 이벤트 발행] eventId={}, scheduleId={}",
            event.eventId(),
            event.scheduleId()
        );
    }
}
