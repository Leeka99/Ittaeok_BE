package com.grepp.spring.app.model.schedule.entity;

import jakarta.persistence.Column;
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
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String eventType;

    private Long scheduleId;

    private LocalDateTime occurredAt;

    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String payload;

    public EventHistory(
        String eventId,
        String eventType,
        Long scheduleId,
        LocalDateTime occurredAt,
        String payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.scheduleId = scheduleId;
        this.occurredAt = occurredAt;
        this.processedAt = LocalDateTime.now();
        this.payload = payload;
    }
}