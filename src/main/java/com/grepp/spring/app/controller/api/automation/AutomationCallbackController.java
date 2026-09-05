package com.grepp.spring.app.controller.api.automation;

import com.grepp.spring.app.model.automation.code.AutomationFailureRequest;
import com.grepp.spring.app.model.automation.code.AutomationSuccessRequest;
import com.grepp.spring.app.model.automation.service.AutomationTaskService;
import com.grepp.spring.app.model.schedule.event.AutomationCompletedEvent;
import com.grepp.spring.infra.kafka.producer.AutomationEventProducer;
import java.time.LocalDateTime;
import java.util.UUID;
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
    private final AutomationEventProducer automationEventProducer;

    @PostMapping("/failure")
    public ResponseEntity<Void> failure(
        @RequestBody AutomationFailureRequest request
    ) {
        automationTaskService.handleFailure(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/success")
    public ResponseEntity<Void> success(
        @RequestBody AutomationSuccessRequest request
    ) {

        AutomationCompletedEvent event =
            new AutomationCompletedEvent(
                UUID.randomUUID().toString(),
                request.scheduleId(),
                LocalDateTime.now()
            );

        automationEventProducer.publishCompleted(event);

        return ResponseEntity.ok().build();
    }

}
