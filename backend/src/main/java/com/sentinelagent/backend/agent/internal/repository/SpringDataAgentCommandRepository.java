package com.sentinelagent.backend.agent.internal.repository;

import com.sentinelagent.backend.agent.internal.domain.AgentCommandDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAgentCommandRepository extends MongoRepository<AgentCommandDocument, String> {
    List<AgentCommandDocument> findByAgentIdOrderByIssuedAtDesc(String agentId);
    List<AgentCommandDocument> findByAgentIdAndStatusOrderByIssuedAtAsc(String agentId, String status);
}
