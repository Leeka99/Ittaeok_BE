package com.grepp.spring.app.model.automation.event;

import java.time.LocalDateTime;

public record AutomationCompletedEvent(
    String eventId,
    Long scheduleId,
    LocalDateTime completedAt
) {

}