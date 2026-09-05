package com.grepp.spring.app.model.automation.service;

import com.grepp.spring.app.model.automation.code.AutomationJobStatus;
import com.grepp.spring.app.model.automation.code.AutomationTrigger;
import com.grepp.spring.app.model.automation.entity.AutomationJob;
import com.grepp.spring.app.model.automation.event.AutomationRequestedEvent;
import com.grepp.spring.app.model.automation.repository.AutomationJobRepository;
import com.grepp.spring.infra.automation.kafka.producer.AutomationEventProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationRetryWorker {

    private final AutomationJobRepository automationJobRepository;
    private final AutomationEventProducer automationEventProducer;

    @Scheduled(fixedDelay = 1000)
    public void retry() {

        List<AutomationJob> jobs =
            automationJobRepository
                .findRetryableJobs(
                    AutomationJobStatus.RETRY_WAIT,
                    LocalDateTime.now(),
                    PageRequest.of(0, 10)
                );

        for (AutomationJob job : jobs) {

            log.info(
                "[재투입 대상 조회] jobId={}, scheduleId={}, retryCount={}",
                job.getId(),
                job.getScheduleId(),
                job.getRetryCount()
            );

            job.processing();
            automationJobRepository.save(job);

            AutomationRequestedEvent event =
                new AutomationRequestedEvent(
                    UUID.randomUUID().toString(),
                    job.getScheduleId(),
                    job.getScheduleName(),
                    job.getStartTime(),
                    job.getEndTime(),
                    AutomationTrigger.RETRY,
                    LocalDateTime.now()
                );

            automationEventProducer.publishRequested(event);
        }
    }
}