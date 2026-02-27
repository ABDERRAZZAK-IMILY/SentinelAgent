package com.sentinelagent.backend.telemetry;

import com.sentinelagent.backend.telemetry.internal.infrastructure.persistence.MetricReportDocument;
import com.sentinelagent.backend.telemetry.internal.infrastructure.persistence.SpringDataMetricReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final SpringDataMetricReportRepository metricReportRepository;

    @GetMapping("/agents/{agentId}/history")
    public ResponseEntity<List<MetricReportDocument>> getHistoricalMetrics(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int hoursBack) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hoursBack);

        List<MetricReportDocument> history = metricReportRepository
                .findByAgentIdAndReceivedAtBetweenOrderByReceivedAtAsc(agentId, start, end);

        return ResponseEntity.ok(history);
    }
}
