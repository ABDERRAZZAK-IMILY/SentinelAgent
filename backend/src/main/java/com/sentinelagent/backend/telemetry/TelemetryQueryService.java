package com.sentinelagent.backend.telemetry;

import com.sentinelagent.backend.telemetry.internal.infrastructure.persistence.MetricReportDocument;
import com.sentinelagent.backend.telemetry.internal.infrastructure.persistence.SpringDataMetricReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryQueryService {
    private final SpringDataMetricReportRepository repository;

    public List<MetricReportDocument> getHistory(String agentId, LocalDateTime from, LocalDateTime to) {
        return repository.findByAgentIdAndReceivedAtBetweenOrderByReceivedAtAsc(agentId, from, to);
    }
}