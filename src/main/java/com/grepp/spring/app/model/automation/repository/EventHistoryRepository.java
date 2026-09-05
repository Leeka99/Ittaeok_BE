package com.grepp.spring.app.model.automation.repository;

import com.grepp.spring.app.model.automation.entity.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Long> {

}
