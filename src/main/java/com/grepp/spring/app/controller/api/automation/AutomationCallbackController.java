package com.grepp.spring.app.controller.api.automation;

import com.grepp.spring.app.model.automation.code.AutomationFailureRequest;
import com.grepp.spring.app.model.automation.code.AutomationSuccessRequest;
import com.grepp.spring.app.model.automation.code.AutomationTrigger;
import com.grepp.spring.app.model.automation.service.AutomationTaskService;
import com.grepp.spring.app.model.automation.event.AutomationCompletedEvent;
import com.grepp.spring.infra.automation.kafka.producer.AutomationEventProducer;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/automation")
@Slf4j
public class AutomationCallbackController {

    private final AutomationTaskService automationTaskService;
    private final AutomationEventProducer automationEventProducer;

    @PostMapping("/failure")
    public ResponseEntity<Void> failure(
        @RequestBody AutomationFailureRequest request
    ) {
        log.info(
            "[자동화 실패(failure) - daily Limit Callback 수신] scheduleId={}",
            request.scheduleId()
        );

        automationTaskService.handleFailure(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/success")
    public ResponseEntity<Void> success(
        @RequestBody AutomationSuccessRequest request
    ) {

        log.info(
            "[자동화 성공(success) Callback 수신] scheduleId={}",
            request.scheduleId()
        );

        automationTaskService.handleSuccess(request.scheduleId());

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
