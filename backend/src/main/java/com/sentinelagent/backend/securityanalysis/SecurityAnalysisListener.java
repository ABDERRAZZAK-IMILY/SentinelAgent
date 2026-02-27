package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.securityanalysis.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.TelemetryReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAnalysisListener {

    private final AnalyzeSecurityUseCase analyzeSecurityUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        log.info("🔔 Security analysis triggered for report: {}", event.reportId());

        try {
            AnalysisResult aiResult = analyzeSecurityUseCase.executeFromEvent(event);
            log.info("✅ Security analysis completed for report: {}", event.reportId());

            String riskLevel = aiResult.riskLevel() != null ? aiResult.riskLevel() : "LOW";

            if (isSuspicious(riskLevel)) {
                SecurityAlertGeneratedEvent alertEvent = new SecurityAlertGeneratedEvent(
                        event.agentId(),
                        riskLevel.toUpperCase(),
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