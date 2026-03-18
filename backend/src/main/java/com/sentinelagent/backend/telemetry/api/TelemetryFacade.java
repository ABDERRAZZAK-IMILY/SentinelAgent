package com.sentinelagent.backend.telemetry.api;

import com.sentinelagent.backend.telemetry.dto.TelemetryAiSummaryResponse;
import com.sentinelagent.backend.telemetry.dto.TelemetryResponse;
import com.sentinelagent.backend.telemetry.internal.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@NamedInterface
public class TelemetryFacade {

    private final TelemetryService telemetryService;

    public List<TelemetryResponse> getHistory(String agentId, LocalDateTime from, LocalDateTime to) {
        return telemetryService.getHistory(agentId, from, to);
    }

    public Optional<TelemetryResponse> getLatest(String agentId) {
        return telemetryService.getLatest(agentId);
    }

    public TelemetryAiSummaryResponse getAiSummary(String agentId, LocalDateTime from, LocalDateTime to) {
        return telemetryService.getAiSummary(agentId, from, to);
    }
}
