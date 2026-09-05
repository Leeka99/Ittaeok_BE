package com.grepp.spring.app.model.automation.event;

import java.time.LocalDateTime;

public record ScheduleConfirmedEvent(
    String eventId,
    Long scheduleId,
    String scheduleName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime occurredAt
) {

}
