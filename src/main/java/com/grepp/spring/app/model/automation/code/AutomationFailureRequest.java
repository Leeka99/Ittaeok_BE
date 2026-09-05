package com.grepp.spring.app.model.automation.code;

public record AutomationFailureRequest(Long scheduleId, String errorType, String message) {

}
