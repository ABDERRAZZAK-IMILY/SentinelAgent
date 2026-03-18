package com.sentinelagent.backend.securityanalysis.internal.listener;

import com.sentinelagent.backend.securityanalysis.event.SecurityAlertGeneratedEvent;
import com.sentinelagent.backend.securityanalysis.internal.domain.AnalysisResult;
import com.sentinelagent.backend.securityanalysis.internal.service.SecurityAnalysisService;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAnalysisListener {

    private final SecurityAnalysisService analysisService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @ApplicationModuleListener
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        log.debug("Received telemetry for security analysis asynchronously: {}", event.agentId());

        try {
            AnalysisResult aiResult = analysisService.analyzeTelemetry(event);
            String severity = aiResult.riskLevel() != null ? aiResult.riskLevel() : "LOW";

            if (isSuspicious(severity)) {
                SecurityAlertGeneratedEvent alertEvent = new SecurityAlertGeneratedEvent(
                        event.agentId(),
                        severity.toUpperCase(),
                        aiResult.threatType(),
                        aiResult.description(),
                        aiResult.recommendation(),
                        LocalDateTime.now());

                eventPublisher.publishEvent(alertEvent);
                log.info("🚨 Published security alert event for agent {}", event.agentId());
            }

        } catch (Exception e) {
            log.error("❌ Security analysis failed for report: {} - {}", event.reportId(), e.getMessage());
        }
    }

    private boolean isSuspicious(String riskLevel) {
        return !riskLevel.equalsIgnoreCase("LOW") &&
                !riskLevel.equalsIgnoreCase("SAFE") &&
                !riskLevel.equalsIgnoreCase("NONE");
    }
}