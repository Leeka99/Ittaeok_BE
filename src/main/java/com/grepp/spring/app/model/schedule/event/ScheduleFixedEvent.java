package com.grepp.spring.app.model.schedule.event;

import java.time.LocalDateTime;

public record ScheduleFixedEvent(
    String eventId,
    Long scheduleId,
    String scheduleName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime occurredAt
) {

}
