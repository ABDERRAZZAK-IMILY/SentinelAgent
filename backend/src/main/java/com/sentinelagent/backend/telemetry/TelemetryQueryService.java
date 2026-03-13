package com.sentinelagent.backend.telemetry;

import com.sentinelagent.backend.telemetry.internal.domain.MetricReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelemetryQueryService {
    private final MetricReportRepository repository;
    private final TelemetryResponseMapper mapper;

    public List<TelemetryResponse> getHistory(String agentId, LocalDateTime from, LocalDateTime to) {
        return repository.findByAgentIdBetween(agentId, from, to).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Optional<TelemetryResponse> getLatest(String agentId) {
        return repository.findLatestByAgentId(agentId)
                .map(mapper::toResponse);
    }

    public TelemetryAiSummaryResponse getAiSummary(String agentId, LocalDateTime from, LocalDateTime to) {
        List<TelemetryResponse> samples = getHistory(agentId, from, to);
        if (samples.isEmpty()) {
            return new TelemetryAiSummaryResponse(
                    agentId,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    "UNKNOWN",
                    "UNKNOWN");
        }

        TelemetryResponse first = samples.get(0);
        TelemetryResponse last = samples.get(samples.size() - 1);

        double avgCpu = samples.stream().mapToDouble(TelemetryResponse::cpuUsage).average().orElse(0);
        double avgRam = samples.stream().mapToDouble(TelemetryResponse::ramUsedPercent).average().orElse(0);
        double avgUploadMbps = samples.stream().mapToDouble(r -> r.bytesSentSec() / 1_000_000.0).average().orElse(0);
        double avgDownloadMbps = samples.stream().mapToDouble(r -> r.bytesRecvSec() / 1_000_000.0).average().orElse(0);

        return new TelemetryAiSummaryResponse(
                agentId,
                samples.size(),
                avgCpu,
                avgRam,
                avgUploadMbps,
                avgDownloadMbps,
                last.cpuUsage(),
                last.ramUsedPercent(),
                toTrend(first.cpuUsage(), last.cpuUsage()),
                toTrend(first.ramUsedPercent(), last.ramUsedPercent()));
    }

    private String toTrend(double first, double last) {
        double delta = last - first;
        if (Math.abs(delta) < 2.0) {
            return "STABLE";
        }
        return delta > 0 ? "RISING" : "FALLING";
    }
}