package com.grepp.spring.app.model.automation.entity;

import com.grepp.spring.app.model.automation.code.AutomationJobStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutomationJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long scheduleId;

    private String scheduleName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AutomationJobStatus status;

    private int retryCount;

    private LocalDateTime nextRetryAt;

    private String lastError;

    public AutomationJob(
        Long scheduleId,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String lastError
    ) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startTime = startTime;
        this.endTime = endTime;

        this.status = AutomationJobStatus.RETRY_WAIT;
        this.retryCount = 0;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(5);
        this.lastError = lastError;
    }

    public void processing() {
        this.status = AutomationJobStatus.PROCESSING;
    }

    public void success() {
        this.status = AutomationJobStatus.SUCCESS;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void retry(String error) {
        this.status = AutomationJobStatus.RETRY_WAIT;
        this.retryCount++;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(5);
        this.lastError = error;
    }

    public void failed(String error) {
        this.status = AutomationJobStatus.FAILED;
        this.lastError = error;
    }
}
