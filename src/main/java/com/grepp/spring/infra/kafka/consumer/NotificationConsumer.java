package com.grepp.spring.infra.kafka.consumer;

import com.grepp.spring.app.model.schedule.entity.Notification;
import com.grepp.spring.app.model.schedule.event.ScheduleConfirmedEvent;
import com.grepp.spring.app.model.schedule.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(
        topics = "schedule-confirmed-events",
        groupId = "notification-group"
    )
    public void consume(ScheduleConfirmedEvent event) {

        String message =
            event.scheduleName() + " 일정이 확정되었습니다.";

        Notification notification =
            new Notification(
                event.scheduleId(),
                message
            );

        notificationRepository.save(notification);

        log.info(
            "[일정 확정 알림 저장] scheduleId={}, message={}",
            event.scheduleId(),
            message
        );
    }
}