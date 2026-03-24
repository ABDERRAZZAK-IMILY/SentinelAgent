package com.sentinelagent.backend.alert.internal.repository;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAlertRepository extends MongoRepository<AlertDocument, String> {
    List<AlertDocument> findByStatus(String status);
    List<AlertDocument> findBySeverity(String severity);
    List<AlertDocument> findBySourceAgentId(String sourceAgentId);
}
