package com.sentinelagent.backend.alert;

import com.sentinelagent.backend.alert.internal.infrastructure.persistence.AlertDocument;
import com.sentinelagent.backend.alert.internal.infrastructure.persistence.SpringDataAlertRepository;
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

    private final SpringDataAlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<List<AlertDocument>> getAllAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        List<AlertDocument> alerts;
        if (status != null) {
            alerts = alertRepository.findByStatus(status.toUpperCase());
        } else if (severity != null) {
            alerts = alertRepository.findBySeverity(severity.toUpperCase());
        } else {
            alerts = alertRepository.findAll();
        }
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertDocument> getAlertById(@PathVariable String alertId) {
        return alertRepository.findById(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{alertId}/status")
    public ResponseEntity<AlertDocument> updateAlertStatus(
            @PathVariable String alertId,
            @RequestParam String status) {
        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(status.toUpperCase());
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getAlertStats() {
        List<AlertDocument> allAlerts = alertRepository.findAll();

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
