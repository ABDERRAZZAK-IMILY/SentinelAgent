package com.sentinelagent.backend.agent.internal.infrastructure.persistence;

import com.sentinelagent.backend.agent.internal.infrastructure.persistence.AgentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAgentRepository extends MongoRepository<AgentDocument, String> {

    Optional<AgentDocument> findByHostname(String hostname);

    Optional<AgentDocument> findByApiKeyHash(String apiKeyHash);

    boolean existsByHostname(String hostname);

    List<AgentDocument> findByStatus(String status);

    long countByStatus(String status);
}
