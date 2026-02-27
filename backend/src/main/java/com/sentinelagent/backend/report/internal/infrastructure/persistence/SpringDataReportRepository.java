package com.sentinelagent.backend.report.internal.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataReportRepository extends MongoRepository<ReportDocument, String> {
    List<ReportDocument> findByAgentIdOrderByGeneratedAtDesc(String agentId);

    List<ReportDocument> findByReportTypeOrderByGeneratedAtDesc(String reportType);
}
