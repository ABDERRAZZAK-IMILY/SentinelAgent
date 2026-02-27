package com.sentinelagent.backend.securityanalysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @ApplicationModuleListener
    public void onTelemetryReceived(TelemetryReceivedEvent event) {
        log.info("🔔 Security analysis triggered for report: {}", event.reportId());

        try {
            String aiResultJson = analyzeSecurityUseCase.executeFromEvent(event);
            log.info("✅ Security analysis completed for report: {}", event.reportId());

            String cleanJson = aiResultJson.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            String riskLevel = root.has("risk_level") ? root.get("risk_level").asText() : "LOW";


            if (!riskLevel.equalsIgnoreCase("LOW") && !riskLevel.equalsIgnoreCase("SAFE")
                    && !riskLevel.equalsIgnoreCase("NONE")) {
                SecurityAlertGeneratedEvent alertEvent = new SecurityAlertGeneratedEvent(
                        event.agentId(),
                        riskLevel.toUpperCase(),
                        root.has("threat_type") ? root.get("threat_type").asText() : "Unknown",
                        root.has("description") ? root.get("description").asText() : "No description provided",
                        root.has("recommendation") ? root.get("recommendation").asText() : "Investigate manually",
                        LocalDateTime.now());
                eventPublisher.publishEvent(alertEvent);
                log.info("🚨 Published security alert event for agent {}", event.agentId());
            }

        } catch (Exception e) {
            log.error("❌ Security analysis failed for report: {} - {}", event.reportId(), e.getMessage(), e);
        }
    }
}
