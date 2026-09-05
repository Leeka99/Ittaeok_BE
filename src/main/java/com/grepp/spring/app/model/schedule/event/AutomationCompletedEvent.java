package com.grepp.spring.app.model.schedule.event;

import java.time.LocalDateTime;

public record AutomationCompletedEvent(
    String eventId,
    Long scheduleId,
    LocalDateTime completedAt
) {

}