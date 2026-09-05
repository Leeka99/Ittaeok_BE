package com.grepp.spring.app.model.automation.event;

import java.time.LocalDateTime;

public record AutomationDeferredEvent(
    String eventId,
    Long scheduleId,
    String reason,
    LocalDateTime nextRetryAt,
    LocalDateTime occurredAt
) {

}