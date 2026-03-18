package com.sentinelagent.backend.report.internal.service;

import com.sentinelagent.backend.alert.api.AlertFacade;
import com.sentinelagent.backend.report.internal.domain.ReportDocument;
import com.sentinelagent.backend.report.internal.repository.SpringDataReportRepository;
import com.sentinelagent.backend.telemetry.api.TelemetryFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String FALLBACK_SUMMARY = "AI analysis is currently unavailable. Review telemetry and alerts manually for this period.";

    private final TelemetryFacade telemetryService;
    private final AlertFacade alertFacade;
    private final SpringDataReportRepository reportRepo;
    private final ChatModel chatModel;

    @Override
    public ReportDocument generateReport(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        return generateReportAsync(agentId, from, to, reportType).join();
    }

    @Override
    @Async
    public CompletableFuture<ReportDocument> generateReportAsync(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        return CompletableFuture.completedFuture(buildAndSaveReport(agentId, from, to, reportType));
    }

    private ReportDocument buildAndSaveReport(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        var metrics = telemetryService.getHistory(agentId, from, to);
        var alerts = alertFacade.getAlertsByAgent(agentId);

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