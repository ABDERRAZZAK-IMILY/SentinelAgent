package com.sentinelagent.backend.telemetry.internal.repository;

import com.sentinelagent.backend.telemetry.internal.domain.MetricReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataMetricReportRepository extends MongoRepository<MetricReportDocument, String> {
    List<MetricReportDocument> findByAgentId(String agentId);

    List<MetricReportDocument> findByHostname(String hostname);

    List<MetricReportDocument> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);

    List<MetricReportDocument> findByAgentIdAndReceivedAtBetweenOrderByReceivedAtAsc(String agentId,
            LocalDateTime start, LocalDateTime end);

    Optional<MetricReportDocument> findTopByAgentIdOrderByReceivedAtDesc(String agentId);
}
