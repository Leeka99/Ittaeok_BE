package com.grepp.spring.app.model.automation.service;

import com.grepp.spring.app.model.automation.code.AutomationFailureRequest;
import com.grepp.spring.app.model.automation.entity.AutomationJob;
import com.grepp.spring.app.model.automation.repository.AutomationJobRepository;
import com.grepp.spring.app.model.n8n.service.N8nService;
import com.grepp.spring.app.model.schedule.entity.Schedule;
import com.grepp.spring.app.model.automation.event.AutomationDeferredEvent;
import com.grepp.spring.app.model.schedule.repository.ScheduleQueryRepository;
import com.grepp.spring.infra.automation.kafka.producer.AutomationEventProducer;
import io.github.bucket4j.Bucket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomationTaskService {

    @Qualifier("automationExecutor")
    private final ThreadPoolTaskExecutor automationExecutor;

    @Qualifier("automationRateLimitBucket")
    private final Bucket automationRateLimitBucket;

    private final N8nService n8nService;

    private final AutomationJobRepository automationJobRepository;

    private final ScheduleQueryRepository scheduleQueryRepository;

    private final AutomationEventProducer automationEventProducer;

    public void submit(
        Long scheduleId,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        try { // Queue에 넣는 것 자체가 실패 - queue 초과
            automationExecutor.execute(() -> {
                try { // Queue에는 들어갔지만 Worker가 처리하다 실패

                    automationRateLimitBucket.asBlocking().consume(1);

                    log.info("[자동화 시작] thread={}, scheduleId={}",
                        Thread.currentThread().getName(), scheduleId);

                    n8nService.sendScheduleConfirmed(
                        scheduleId, scheduleName, startTime, endTime);

                    log.info("[자동화 완료] scheduleId={}", scheduleId);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    log.warn(
                        "[자동화 Rate Limit 대기 중 인터럽트] scheduleId={}",
                        scheduleId
                    );
                } catch (Exception e) {

                    log.error("[자동화 실패] scheduleId={}, error={}",
                        scheduleId, e.getMessage());

                    throw e;
                }
            });
        } catch (TaskRejectedException e) {

            log.error(
                "[자동화 Queue 초과 감지] scheduleId={},",
                scheduleId
            );

            AutomationJob job = new AutomationJob(
                scheduleId,
                scheduleName,
                startTime,
                endTime,
                e.getMessage()
            );

            log.info("[AutomationJob 저장 직전] scheduleId={}", scheduleId);

            AutomationJob savedJob = automationJobRepository.save(job);

            log.info(
                "[AutomationJob 저장 완료] jobId={}, scheduleId={}",
                savedJob.getId(),
                scheduleId
            );
        }

    }

    public void retry(Long jobId) {

        AutomationJob job = automationJobRepository.findById(jobId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "AutomationJob을 찾을 수 없습니다. jobId=" + jobId
                )
            );

        // 비동기 작업에 사용할 값만 복사
        Long scheduleId = job.getScheduleId();
        String scheduleName = job.getScheduleName();
        LocalDateTime startTime = job.getStartTime();
        LocalDateTime endTime = job.getEndTime();

        try {
            // RetryWorker가 같은 작업을 또 가져가지 못하도록 변경
            job.processing();
            automationJobRepository.save(job);

            automationExecutor.execute(() -> {

                try {
                    automationRateLimitBucket
                        .asBlocking()
                        .consume(1);

                    log.info(
                        "[자동화 재투입 시작] jobId={}, scheduleId={}",
                        jobId,
                        scheduleId
                    );

                    n8nService.sendScheduleConfirmed(
                        scheduleId,
                        scheduleName,
                        startTime,
                        endTime
                    );

                    AutomationJob successJob =
                        automationJobRepository.findById(jobId)
                            .orElseThrow();

                    successJob.success();
                    automationJobRepository.save(successJob);

                    log.info(
                        "[자동화 재투입 성공] jobId={}, scheduleId={}",
                        jobId,
                        scheduleId
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    AutomationJob retryJob =
                        automationJobRepository.findById(jobId)
                            .orElseThrow();

                    retryJob.retry(e.getMessage());
                    automationJobRepository.save(retryJob);

                } catch (Exception e) {

                    AutomationJob retryJob =
                        automationJobRepository.findById(jobId)
                            .orElseThrow();

                    retryJob.retry(e.getMessage());
                    automationJobRepository.save(retryJob);

                    log.error(
                        "[자동화 재투입 실패] jobId={}, scheduleId={}",
                        jobId,
                        scheduleId,
                        e
                    );
                }
            });

        } catch (TaskRejectedException e) {

            job.retry(e.getMessage());
            automationJobRepository.save(job);

            log.warn(
                "[자동화 재투입 Queue 초과] jobId={}, scheduleId={}, retryCount={}, nextRetryAt={}",
                job.getId(),
                job.getScheduleId(),
                job.getRetryCount(),
                job.getNextRetryAt()
            );
        }
    }
    @Transactional
    public void handleFailure(AutomationFailureRequest request) {

        Long scheduleId = request.getScheduleId();

        List<AutomationJob> jobs = automationJobRepository
            .findRecentScheduleJobs(scheduleId, PageRequest.of(0, 1));

        AutomationJob job = jobs.stream().findFirst()
            .orElseGet(() -> createAutomationJob(scheduleId, request.getMessage()));

        if ("ZOOM_DAILY_LIMIT".equals(request.getErrorType())) {

            job.retryNextDay(request.getMessage());

            AutomationDeferredEvent event =
                new AutomationDeferredEvent(
                    UUID.randomUUID().toString(),
                    scheduleId,
                    request.getMessage(),
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

            job.failed(request.getMessage());

            log.error(
                "[자동화 최종 실패] scheduleId={}, errorType={}, message={}",
                scheduleId,
                request.getErrorType(),
                request.getMessage()
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

}
