package com.grepp.spring.app.model.schedule.entity;

import jakarta.persistence.Entity;
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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long scheduleId;

    private String message;

    private boolean readStatus;

    private LocalDateTime createdAt;

    public Notification(Long scheduleId, String message) {
        this.scheduleId = scheduleId;
        this.message = message;
        this.readStatus = false;
        this.createdAt = LocalDateTime.now();
    }
}