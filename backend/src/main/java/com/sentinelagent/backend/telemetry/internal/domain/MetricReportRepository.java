package com.sentinelagent.backend.telemetry.internal.domain;

import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.MetricReportId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


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
