package com.sentinelagent.backend.domain.agent.exception;

import com.sentinelagent.backend.domain.common.DomainException;

/**
 * Exception thrown when an Agent is not found
 */
public class AgentNotFoundException extends DomainException {

    public AgentNotFoundException(String agentId) {
        super("Agent not found with ID: " + agentId);
    }

    public AgentNotFoundException(String field, String value) {
        super("Agent not found with " + field + ": " + value);
    }
}
