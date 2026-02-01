package com.sentinelagent.backend.domain.agent;

/**
 * AgentStatus Enum - Represents the lifecycle states of an Agent.
 * Part of the Domain Layer.
 */
public enum AgentStatus {

    /**
     * Agent is registered and actively sending data
     */
    ACTIVE,

    /**
     * Agent hasn't sent heartbeat within threshold period
     */
    INACTIVE,

    /**
     * Agent's API key has been revoked by admin
     */
    REVOKED,

    /**
     * Agent is reporting errors or in error state
     */
    ERROR
}
