package com.grepp.spring.app.model.schedule.repository;

import com.grepp.spring.app.model.schedule.entity.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Long> {

}
