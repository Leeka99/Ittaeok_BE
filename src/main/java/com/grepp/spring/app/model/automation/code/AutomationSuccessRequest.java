package com.grepp.spring.app.model.automation.code;

import java.time.LocalDateTime;

public record AutomationSuccessRequest(Long scheduleId, String scheduleName,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime) {

}
