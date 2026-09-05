package com.grepp.spring.app.model.schedule.event;

import java.time.LocalDateTime;

public record AutomationDeferredEvent(
    String eventId,
    Long scheduleId,
    String reason,
    LocalDateTime nextRetryAt,
    LocalDateTime occurredAt
) {

}