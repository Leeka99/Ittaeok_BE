package com.grepp.spring.app.model.automation.code;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AutomationFailureRequest {
    private Long scheduleId;
    private String errorType;
    private String message;
}
