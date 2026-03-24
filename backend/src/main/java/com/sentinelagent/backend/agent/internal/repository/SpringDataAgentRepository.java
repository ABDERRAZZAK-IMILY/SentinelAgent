package com.sentinelagent.backend.agent.internal.repository;

import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAgentRepository extends MongoRepository<AgentDocument, String> {

    boolean existsByHostname(String hostname);

    List<AgentDocument> findByStatus(String status);

    long countByStatus(String status);
}
