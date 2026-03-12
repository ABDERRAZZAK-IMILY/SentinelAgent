package com.sentinelagent.backend.agent.internal.domain;

import java.util.List;
import java.util.Optional;


public interface AgentRepository {


    Agent save(Agent agent);


    Optional<Agent> findById(AgentId id);


    Optional<Agent> findByApiKeyHash(String apiKeyHash);


    Optional<Agent> findByHostname(String hostname);


    boolean existsByHostname(String hostname);


    List<Agent> findByStatus(AgentStatus status);


    List<Agent> findAll();


    void deleteById(AgentId id);

    long countByStatus(AgentStatus status);
}
