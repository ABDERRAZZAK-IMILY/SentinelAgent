package com.sentinelagent.backend.report;

import com.sentinelagent.backend.report.internal.infrastructure.persistence.ReportDocument;
import com.sentinelagent.backend.report.internal.infrastructure.persistence.SpringDataReportRepository;
import com.sentinelagent.backend.telemetry.TelemetryQueryService;
import com.sentinelagent.backend.alert.GetAlertsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateAiReportUseCase {

    private static final String FALLBACK_SUMMARY = "AI analysis is currently unavailable. Review telemetry and alerts manually for this period.";

    private final TelemetryQueryService telemetryService;
    private final GetAlertsUseCase alertsUseCase;
    private final SpringDataReportRepository reportRepo;
    private final ChatModel chatModel;

    public ReportDocument execute(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        var metrics = telemetryService.getHistory(agentId, from, to);
        var alerts = alertsUseCase.getAlertsByAgent(agentId);

        String prompt = String.format("""
            You are a Senior Cybersecurity Analyst for SentinelAgent.
            Create a concise but actionable AI security report for agent: %s.

            PERIOD: From %s To %s
            TOTAL METRIC SAMPLES: %d
            METRICS DATA: %s
            TOTAL SECURITY ALERTS: %d
            ALERTS DATA: %s

            INSTRUCTIONS:
            1. Start with a one-line risk verdict: LOW, MEDIUM, or HIGH.
            2. Summarize system security posture from telemetry and alerts.
            3. Highlight suspicious patterns and probable root causes.
            4. Provide 3 practical recommendations prioritized for operations.
            5. If data is insufficient, clearly say what is missing.
            """, agentId, from, to, metrics.size(), metrics, alerts.size(), alerts);

        String aiResponse;
        try {
            aiResponse = chatModel.call(prompt);
            if (aiResponse == null || aiResponse.isBlank()) {
                aiResponse = FALLBACK_SUMMARY;
            }
        } catch (Exception ex) {
            log.error("AI report generation failed for agent {}", agentId, ex);
            aiResponse = FALLBACK_SUMMARY;
        }

        ReportDocument saved = reportRepo.save(ReportDocument.builder()
                .agentId(agentId)
                .reportType(reportType)
                .aiSummary(aiResponse)
                .generatedAt(LocalDateTime.now())
                .fromDate(from)
                .toDate(to)
                .build());

        saved.setContentUrl("/api/v1/reports/download/" + saved.getId());
        return reportRepo.save(saved);
    }
}