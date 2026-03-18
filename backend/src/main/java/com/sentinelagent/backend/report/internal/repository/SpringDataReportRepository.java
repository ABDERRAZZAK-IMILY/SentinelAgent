package com.sentinelagent.backend.report.internal.repository;

import com.sentinelagent.backend.report.internal.domain.ReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataReportRepository extends MongoRepository<ReportDocument, String> {
    List<ReportDocument> findAllByOrderByGeneratedAtDesc();

    List<ReportDocument> findByAgentIdOrderByGeneratedAtDesc(String agentId);

    List<ReportDocument> findByReportTypeOrderByGeneratedAtDesc(String reportType);
}
