package com.sentinelagent.backend.alert.internal.controller;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import com.sentinelagent.backend.alert.internal.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertDocument>> getAllAlerts(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "severity", required = false) String severity) {
        return ResponseEntity.ok(alertService.getAlerts(status, severity));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertDocument> getAlertById(@PathVariable("alertId") String alertId) {
        return alertService.getById(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{alertId}/status")
    public ResponseEntity<AlertDocument> updateAlertStatus(
            @PathVariable("alertId") String alertId,
            @RequestParam(name = "status") String status) {
        AlertDocument updated = alertService.updateStatus(alertId, status);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getAlertStats() {
        List<AlertDocument> allAlerts = alertService.getAlerts(null, null);

        // Count by severity
        Map<String, Long> severityCounts = allAlerts.stream()
                .collect(Collectors.groupingBy(AlertDocument::getSeverity, Collectors.counting()));

        // Count by status
        Map<String, Long> statusCounts = allAlerts.stream()
                .collect(Collectors.groupingBy(AlertDocument::getStatus, Collectors.counting()));

        // Combine stats
        return ResponseEntity.ok(Map.of(
                "CRITICAL", severityCounts.getOrDefault("CRITICAL", 0L),
                "HIGH", severityCounts.getOrDefault("HIGH", 0L),
                "MEDIUM", severityCounts.getOrDefault("MEDIUM", 0L),
                "LOW", severityCounts.getOrDefault("LOW", 0L),
                "NEW", statusCounts.getOrDefault("NEW", 0L),
                "REVIEWED", statusCounts.getOrDefault("REVIEWED", 0L),
                "RESOLVED", statusCounts.getOrDefault("RESOLVED", 0L)));
    }
}
