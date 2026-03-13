package com.sentinelagent.backend.telemetry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryQueryService telemetryQueryService;

    @GetMapping("/agents/{agentId}/history")
    public ResponseEntity<List<TelemetryResponse>> getHistoricalMetrics(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int hoursBack) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hoursBack);

        List<TelemetryResponse> history = telemetryQueryService.getHistory(agentId, start, end);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/agents/{agentId}/latest")
    public ResponseEntity<TelemetryResponse> getLatestMetrics(@PathVariable String agentId) {
        return telemetryQueryService.getLatest(agentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agents/{agentId}/ai-summary")
    public ResponseEntity<TelemetryAiSummaryResponse> getAiSummary(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "6") int hoursBack) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(Math.max(hoursBack, 1));

        TelemetryAiSummaryResponse summary = telemetryQueryService.getAiSummary(agentId, start, end);
        return ResponseEntity.ok(summary);
    }
}
