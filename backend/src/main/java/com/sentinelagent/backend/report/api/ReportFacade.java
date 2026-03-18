package com.sentinelagent.backend.report.api;

import com.sentinelagent.backend.report.internal.domain.ReportDocument;
import com.sentinelagent.backend.report.internal.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@NamedInterface
public class ReportFacade {

    private final ReportService reportService;

    public ReportDocument generateReport(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        return reportService.generateReport(agentId, from, to, reportType);
    }

    public CompletableFuture<ReportDocument> generateReportAsync(String agentId, LocalDateTime from, LocalDateTime to, String reportType) {
        return reportService.generateReportAsync(agentId, from, to, reportType);
    }
}
