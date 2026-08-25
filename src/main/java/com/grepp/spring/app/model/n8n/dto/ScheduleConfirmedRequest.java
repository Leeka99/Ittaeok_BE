package com.grepp.spring.app.model.n8n.dto;

import java.time.LocalDateTime;

public record ScheduleConfirmedRequest(
    Long scheduleId,
    String scheduleName,
    LocalDateTime startTime,
    LocalDateTime endTime
) {

}
