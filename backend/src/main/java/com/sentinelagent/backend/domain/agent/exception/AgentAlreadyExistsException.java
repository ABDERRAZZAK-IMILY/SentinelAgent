package com.sentinelagent.backend.domain.agent.exception;

import com.sentinelagent.backend.domain.common.DomainException;

/**
 * Exception thrown when an Agent already exists (duplicate registration)
 */
public class AgentAlreadyExistsException extends DomainException {

    public AgentAlreadyExistsException(String hostname) {
        super("Agent already registered with hostname: " + hostname);
    }
}
