package com.sentinelagent.backend.domain.agent.exception;

import com.sentinelagent.backend.domain.common.DomainException;

/**
 * Exception thrown when Agent authentication fails
 */
public class InvalidAgentCredentialsException extends DomainException {

    public InvalidAgentCredentialsException() {
        super("Invalid agent API key");
    }

    public InvalidAgentCredentialsException(String reason) {
        super("Agent authentication failed: " + reason);
    }
}
