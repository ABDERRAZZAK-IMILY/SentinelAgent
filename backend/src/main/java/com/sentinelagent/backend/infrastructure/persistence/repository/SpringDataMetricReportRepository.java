package com.sentinelagent.backend.infrastructure.persistence.repository;

import com.sentinelagent.backend.infrastructure.persistence.entity.MetricReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data MongoDB Repository for MetricReport documents.
 */
public interface SpringDataMetricReportRepository extends MongoRepository<MetricReportDocument, String> {
    List<MetricReportDocument> findByAgentId(String agentId);

    List<MetricReportDocument> findByHostname(String hostname);

    List<MetricReportDocument> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);
}
