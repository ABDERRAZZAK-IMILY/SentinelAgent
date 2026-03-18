package com.sentinelagent.backend.report.internal.controller;

import com.sentinelagent.backend.report.internal.domain.ReportDocument;
import com.sentinelagent.backend.report.internal.repository.SpringDataReportRepository;
import com.sentinelagent.backend.report.internal.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final SpringDataReportRepository reportRepository;
    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@RequestBody GenerateReportRequest request) {
        if (request == null || request.agentId() == null || request.agentId().isBlank()) {
            return ResponseEntity.badRequest().body("agentId is required.");
        }

        LocalDateTime to = parseOrDefault(request.toDate(), LocalDateTime.now());
        LocalDateTime from = parseOrDefault(request.fromDate(), to.minusHours(24));
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("fromDate must be before toDate.");
        }

        String reportType = request.reportType() == null || request.reportType().isBlank()
                ? "AI_SECURITY_SUMMARY"
                : request.reportType().trim();

        ReportDocument generated = reportService.generateReport(request.agentId().trim(), from, to, reportType);
        return ResponseEntity.ok(generated);
    }

    @GetMapping
    public ResponseEntity<List<ReportDocument>> getAllReports(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String reportType) {

        if (agentId != null && !agentId.isBlank()) {
            return ResponseEntity.ok(reportRepository.findByAgentIdOrderByGeneratedAtDesc(agentId));
        }
        if (reportType != null && !reportType.isBlank()) {
            return ResponseEntity.ok(reportRepository.findByReportTypeOrderByGeneratedAtDesc(reportType));
        }
        return ResponseEntity.ok(reportRepository.findAllByOrderByGeneratedAtDesc());
    }

    @GetMapping("/download/{reportId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String reportId) {
        return reportRepository.findById(reportId)
                .map(report -> {
                    String body = buildDownloadContent(report);
                    String fileName = "report-" + reportId + ".txt";
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                            .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                            .body(body.getBytes(StandardCharsets.UTF_8));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private LocalDateTime parseOrDefault(String raw, LocalDateTime fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ex) {
            log.warn("Invalid report date: {}", raw, ex);
            return fallback;
        }
    }

    private String buildDownloadContent(ReportDocument report) {
        return String.format(
                "SentinelAgent AI Report\n" +
                        "=======================\n" +
                        "Report ID: %s\n" +
                        "Agent ID: %s\n" +
                        "Type: %s\n" +
                        "Generated At: %s\n" +
                        "Period: %s -> %s\n\n" +
                        "AI Summary:\n%s\n",
                valueOrUnknown(report.getId()),
                valueOrUnknown(report.getAgentId()),
                valueOrUnknown(report.getReportType()),
                valueOrUnknown(report.getGeneratedAt() != null ? report.getGeneratedAt().toString() : null),
                valueOrUnknown(report.getFromDate() != null ? report.getFromDate().toString() : null),
                valueOrUnknown(report.getToDate() != null ? report.getToDate().toString() : null),
                valueOrUnknown(report.getAiSummary()));
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    public record GenerateReportRequest(String agentId, String reportType, String fromDate, String toDate) {
    }
}
