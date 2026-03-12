package com.sentinelagent.backend.report;

import com.sentinelagent.backend.report.internal.infrastructure.persistence.ReportDocument;
import com.sentinelagent.backend.report.internal.infrastructure.persistence.SpringDataReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SpringDataReportRepository reportRepository;

    @PostMapping("/generate")
    public ResponseEntity<ReportDocument> generateGlobalReport(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {


  // moke peport
        ReportDocument report = ReportDocument.builder()
                .reportType("GLOBAL_SUMMARY")
                .contentUrl("/downloads/mock-report-" + UUID.randomUUID() + ".pdf")
                .generatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(reportRepository.save(report));
    }

    @GetMapping
    public ResponseEntity<List<ReportDocument>> getAllReports() {
        return ResponseEntity.ok(reportRepository.findAll());
    }

    @GetMapping("/download/{reportId}")
    public ResponseEntity<String> downloadReport(@PathVariable String reportId) {
        return reportRepository.findById(reportId)
                .map(report -> ResponseEntity.ok("Simulating download of " + report.getContentUrl()))
                .orElse(ResponseEntity.notFound().build());
    }
}
