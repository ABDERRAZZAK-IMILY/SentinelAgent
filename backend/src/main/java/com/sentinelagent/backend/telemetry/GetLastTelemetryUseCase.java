package com.sentinelagent.backend.telemetry;

import com.sentinelagent.backend.telemetry.internal.domain.MetricReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GetLastTelemetryUseCase {

    private final MetricReportRepository metricReportRepository;

    public MetricReport getLastTelemetry(String agentId) {
        return metricReportRepository.findLatestByAgentId(agentId)
                .orElse(null);
    }
}
