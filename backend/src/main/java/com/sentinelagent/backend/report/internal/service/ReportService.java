package com.sentinelagent.backend.report.internal.service;

import com.sentinelagent.backend.report.internal.domain.ReportDocument;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface ReportService {
    ReportDocument generateReport(String agentId, LocalDateTime from, LocalDateTime to, String reportType);
    CompletableFuture<ReportDocument> generateReportAsync(String agentId, LocalDateTime from, LocalDateTime to, String reportType);
}

