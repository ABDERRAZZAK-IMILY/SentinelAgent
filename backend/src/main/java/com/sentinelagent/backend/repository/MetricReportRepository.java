package com.sentinelagent.backend.repository;

import com.sentinelagent.backend.model.MetricReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricReportRepository extends MongoRepository<MetricReport, String> {
}