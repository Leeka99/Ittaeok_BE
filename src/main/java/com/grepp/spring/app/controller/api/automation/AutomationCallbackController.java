package com.grepp.spring.app.controller.api.automation;

import com.grepp.spring.app.model.automation.code.AutomationFailureRequest;
import com.grepp.spring.app.model.automation.service.AutomationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/automation")
public class AutomationCallbackController {

    private final AutomationTaskService automationTaskService;

    @PostMapping("/failure")
    public ResponseEntity<Void> failure(
        @RequestBody AutomationFailureRequest request
    ) {
        automationTaskService.handleFailure(request);

        return ResponseEntity.ok().build();
    }

}
