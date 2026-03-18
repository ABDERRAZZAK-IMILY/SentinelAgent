package com.sentinelagent.backend.telemetry.internal.service;

import com.sentinelagent.backend.telemetry.dto.TelemetryAiSummaryResponse;
import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.telemetry.dto.TelemetryResponse;
import com.sentinelagent.backend.telemetry.internal.domain.MetricReport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TelemetryService {
    MetricReport processAndSaveTelemetry(TelemetryData data);
    List<TelemetryResponse> getHistory(String agentId, LocalDateTime from, LocalDateTime to);
    Optional<TelemetryResponse> getLatest(String agentId);
    TelemetryAiSummaryResponse getAiSummary(String agentId, LocalDateTime from, LocalDateTime to);
}

