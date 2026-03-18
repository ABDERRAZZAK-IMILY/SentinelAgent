package com.sentinelagent.backend.alert.internal.listener;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import com.sentinelagent.backend.alert.internal.service.AlertService;
import com.sentinelagent.backend.securityanalysis.event.SecurityAlertGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAlertListener {

    private final AlertService alertService;

    @ApplicationModuleListener
    @EventListener // fallback to plain Spring events to ensure handling
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

        alertService.save(alert);
        log.info("💾 Alert saved to database with ID: {}", alert.getId());
    }
}
