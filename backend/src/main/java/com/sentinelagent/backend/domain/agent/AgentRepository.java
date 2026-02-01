package com.sentinelagent.backend.domain.agent;

import java.util.List;
import java.util.Optional;

/**
 * AgentRepository Port - Interface defining persistence operations for Agents.
 * Part of the Domain Layer - implemented by Infrastructure layer.
 */
public interface AgentRepository {

    /**
     * Save or update an Agent
     */
    Agent save(Agent agent);

    /**
     * Find an Agent by its ID
     */
    Optional<Agent> findById(AgentId id);

    /**
     * Find an Agent by its hashed API key
     */
    Optional<Agent> findByApiKeyHash(String apiKeyHash);

    /**
     * Find an Agent by hostname
     */
    Optional<Agent> findByHostname(String hostname);

    /**
     * Check if an Agent exists with the given hostname
     */
    boolean existsByHostname(String hostname);

    /**
     * Find all Agents with a specific status
     */
    List<Agent> findByStatus(AgentStatus status);

    /**
     * Find all registered Agents
     */
    List<Agent> findAll();

    /**
     * Delete an Agent by ID
     */
    void deleteById(AgentId id);

    /**
     * Count Agents by status
     */
    long countByStatus(AgentStatus status);
}
