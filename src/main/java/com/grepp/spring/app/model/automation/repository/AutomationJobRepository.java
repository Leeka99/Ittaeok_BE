package com.grepp.spring.app.model.automation.repository;

import com.grepp.spring.app.model.automation.code.AutomationJobStatus;
import com.grepp.spring.app.model.automation.entity.AutomationJob;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AutomationJobRepository  extends JpaRepository<AutomationJob, Long> {
    @Query("""
    SELECT j
    FROM AutomationJob j
    WHERE j.status = :status
      AND j.nextRetryAt <= :now
    ORDER BY j.nextRetryAt ASC
""")
    List<AutomationJob> findRetryableJobs(
        @Param("status") AutomationJobStatus status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
}
