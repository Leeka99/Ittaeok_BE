package com.grepp.spring.app.model.automation.service;

import com.grepp.spring.app.model.automation.code.AutomationFailureRequest;
import com.grepp.spring.app.model.automation.code.AutomationJobStatus;
import com.grepp.spring.app.model.automation.entity.AutomationJob;
import com.grepp.spring.app.model.automation.event.AutomationDeferredEvent;
import com.grepp.spring.app.model.automation.repository.AutomationJobRepository;
import com.grepp.spring.app.model.schedule.entity.Schedule;
import com.grepp.spring.app.model.schedule.repository.ScheduleQueryRepository;
import com.grepp.spring.infra.automation.kafka.producer.AutomationEventProducer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationTaskService {

    private final AutomationJobRepository automationJobRepository;

    private final ScheduleQueryRepository scheduleQueryRepository;

    private final AutomationEventProducer automationEventProducer;

    @Transactional
    public void handleFailure(AutomationFailureRequest request) {

        Long scheduleId = request.scheduleId();

        List<AutomationJob> jobs = automationJobRepository
            .findRecentScheduleJobs(scheduleId, PageRequest.of(0, 1));

        AutomationJob job = jobs.stream().findFirst()
            .orElseGet(() -> createAutomationJob(scheduleId, request.message()));

        if ("ZOOM_DAILY_LIMIT".equals(request.errorType())) {

            job.retryNextDay(request.message());

            AutomationDeferredEvent event =
                new AutomationDeferredEvent(
                    UUID.randomUUID().toString(),
                    scheduleId,
                    request.message(),
                    job.getNextRetryAt(),
                    LocalDateTime.now()
                );

            automationEventProducer.publishDeferred(event);

            log.warn(
                "[Zoom 일일 한도 초과] scheduleId={}, nextRetryAt={}",
                scheduleId,
                job.getNextRetryAt()
            );

        } else {

            job.failed(request.message());

            log.error(
                "[자동화 최종 실패] scheduleId={}, errorType={}, message={}",
                scheduleId,
                request.errorType(),
                request.message()
            );
        }
    }

    private AutomationJob createAutomationJob(
        Long scheduleId,
        String error
    ) {
        Schedule schedule = scheduleQueryRepository.findById(scheduleId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Schedule을 찾을 수 없습니다. scheduleId=" + scheduleId
                )
            );

        AutomationJob job = new AutomationJob(
            schedule.getId(),
            schedule.getScheduleName(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            error
        );

        return automationJobRepository.save(job);
    }

    @Transactional
    public void handleSuccess(Long scheduleId) {

        List<AutomationJob> jobs =
            automationJobRepository.findRecentScheduleJobs(
                scheduleId,
                PageRequest.of(0, 1)
            );

        if (jobs.isEmpty()) {
            return;
        }

        AutomationJob job = jobs.get(0);

        if (job.getStatus() != AutomationJobStatus.PROCESSING) {
            return;
        }

        job.success();

        log.info(
            "[자동화 Job 성공 처리] jobId={}, scheduleId={}, status={}",
            job.getId(),
            job.getScheduleId(),
            job.getStatus()
        );
    }
}