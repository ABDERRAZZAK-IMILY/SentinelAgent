package com.sentinelagent.backend.agent.internal.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAgentCommandRepository extends MongoRepository<AgentCommandDocument, String> {
    List<AgentCommandDocument> findByAgentIdOrderByIssuedAtDesc(String agentId);

    List<AgentCommandDocument> findByAgentIdAndStatus(String agentId, String status);
}
