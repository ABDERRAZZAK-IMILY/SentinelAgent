package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.shared.exception.DomainException;

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
