package com.sentinelagent.backend.domain.telemetry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Port for MetricReport persistence.
 * Part of the Domain Layer - defines the contract for infrastructure
 * implementation.
 */
public interface MetricReportRepository {

    MetricReport save(MetricReport report);

    Optional<MetricReport> findById(MetricReportId id);

    List<MetricReport> findByAgentId(String agentId);

    List<MetricReport> findByHostname(String hostname);

    List<MetricReport> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);

    List<MetricReport> findAll();

    void deleteById(MetricReportId id);

    long count();
}
