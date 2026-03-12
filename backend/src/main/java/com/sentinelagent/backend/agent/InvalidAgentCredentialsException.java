package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.shared.exception.DomainException;


public class InvalidAgentCredentialsException extends DomainException {

    public InvalidAgentCredentialsException() {
        super("Invalid agent API key");
    }

    public InvalidAgentCredentialsException(String reason) {
        super("Agent authentication failed: " + reason);
    }
}
