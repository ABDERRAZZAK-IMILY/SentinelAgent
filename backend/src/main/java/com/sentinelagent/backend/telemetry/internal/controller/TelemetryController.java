package com.sentinelagent.backend.telemetry.internal.controller;

import com.sentinelagent.backend.telemetry.dto.TelemetryAiSummaryResponse;
import com.sentinelagent.backend.telemetry.dto.TelemetryResponse;
import com.sentinelagent.backend.telemetry.internal.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/agents/{agentId}/history")
    public ResponseEntity<List<TelemetryResponse>> getHistoricalMetrics(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int hoursBack) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hoursBack);

        List<TelemetryResponse> history = telemetryService.getHistory(agentId, start, end);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/agents/{agentId}/latest")
    public ResponseEntity<TelemetryResponse> getLatestMetrics(@PathVariable String agentId) {
        return telemetryService.getLatest(agentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agents/{agentId}/ai-summary")
    public ResponseEntity<TelemetryAiSummaryResponse> getAiSummary(
            @PathVariable String agentId,
            @RequestParam(defaultValue = "6") int hoursBack) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(Math.max(hoursBack, 1));

        TelemetryAiSummaryResponse summary = telemetryService.getAiSummary(agentId, start, end);
        return ResponseEntity.ok(summary);
    }
}
