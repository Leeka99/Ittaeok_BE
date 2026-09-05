package com.grepp.spring.app.model.automation.repository;

import com.grepp.spring.app.model.automation.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
