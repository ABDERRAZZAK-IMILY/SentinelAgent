package com.sentinelagent.backend.report;

import com.sentinelagent.backend.report.internal.infrastructure.persistence.ReportDocument;
import com.sentinelagent.backend.report.internal.infrastructure.persistence.SpringDataReportRepository;
import com.sentinelagent.backend.telemetry.TelemetryQueryService;
import com.sentinelagent.backend.alert.GetAlertsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GenerateAiReportUseCase {

    private final TelemetryQueryService telemetryService;
    private final GetAlertsUseCase alertsUseCase;
    private final SpringDataReportRepository reportRepo;
    private final ChatModel chatModel;

    public ReportDocument execute(String agentId, LocalDateTime from, LocalDateTime to) {
        var metrics = telemetryService.getHistory(agentId, from, to);
        var alerts = alertsUseCase.getAlertsByAgent(agentId);

        String prompt = String.format("""
            You are a Senior Cybersecurity Analyst. Generate a detailed security summary for Agent: %s.
            
            PERIOD: From %s To %s
            HISTORICAL METRICS: %s
            SECURITY ALERTS DETECTED: %s
            
            INSTRUCTIONS:
            1. Summarize the overall health of the system.
            2. Identify patterns of suspicious behavior based on the timeline.
            3. Provide strategic recommendations for the IT department.
            4. Output the report in professional Arabic language.
            """, agentId, from, to, metrics.toString(), alerts.toString());

        String aiResponse = chatModel.call(prompt);

        return reportRepo.save(ReportDocument.builder()
                .agentId(agentId)
                .aiSummary(aiResponse)
                .generatedAt(LocalDateTime.now())
                .build());
    }
}