package com.grepp.spring.app.model.n8n.service;

import com.grepp.spring.app.model.n8n.dto.ScheduleConfirmedRequest;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class N8nService {
    private final RestClient restClient;
    private final String webhookUrl;
    public N8nService(
        RestClient.Builder restClientBuilder,
        @Value("${n8n.webhook.schedule-confirmed}") String webhookUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    public void sendScheduleConfirmed(
        Long scheduleId,
        String scheduleName,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {

        ScheduleConfirmedRequest request =
            new ScheduleConfirmedRequest(
                scheduleId,
                scheduleName,
                startTime,
                endTime
            );

        restClient.post()
            .uri(webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}