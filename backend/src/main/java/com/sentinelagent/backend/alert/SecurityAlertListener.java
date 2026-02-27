package com.sentinelagent.backend.alert;

import com.sentinelagent.backend.alert.internal.infrastructure.persistence.AlertDocument;
import com.sentinelagent.backend.alert.internal.infrastructure.persistence.SpringDataAlertRepository;
import com.sentinelagent.backend.securityanalysis.SecurityAlertGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAlertListener {

    private final SpringDataAlertRepository alertRepository;

    @ApplicationModuleListener
    public void onSecurityAlertGenerated(SecurityAlertGeneratedEvent event) {
        log.info("🔴 Received SecurityAlertGeneratedEvent for agent: {}", event.agentId());

        AlertDocument alert = AlertDocument.builder()
                .severity(event.severity())
                .threatType(event.threatType())
                .description(event.description())
                .recommendation(event.recommendation())
                .sourceAgentId(event.agentId())
                .status("NEW")
                .timestamp(event.timestamp())
                .build();

        alertRepository.save(alert);
        log.info("💾 Alert saved to database with ID: {}", alert.getId());
    }
}
