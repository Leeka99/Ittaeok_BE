package com.grepp.spring.app.model.automation.event;

import com.grepp.spring.app.model.automation.code.AutomationTrigger;
import java.time.LocalDateTime;

public record AutomationRequestedEvent(String eventId,
                                       Long scheduleId,
                                       String scheduleName,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime,
                                       AutomationTrigger trigger,
                                       LocalDateTime occurredAt) {

}
