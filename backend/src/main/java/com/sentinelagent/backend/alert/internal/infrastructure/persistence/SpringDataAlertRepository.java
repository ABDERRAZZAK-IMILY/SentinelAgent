package com.sentinelagent.backend.alert.internal.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAlertRepository extends MongoRepository<AlertDocument, String> {
    List<AlertDocument> findBySourceAgentIdOrderByTimestampDesc(String sourceAgentId);

    List<AlertDocument> findByStatus(String status);

    List<AlertDocument> findBySeverity(String severity);
}
