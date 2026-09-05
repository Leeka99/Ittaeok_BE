package com.grepp.spring.app.model.automation.code;

import java.time.LocalDateTime;

public record AutomationCompletedEvent(String eventId,
                                       Long scheduleId,
                                       LocalDateTime occurredAt) {

}
