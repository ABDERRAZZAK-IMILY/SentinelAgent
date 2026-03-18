package com.sentinelagent.backend.agent.api;

import com.sentinelagent.backend.shared.exception.DomainException;


public class InvalidAgentCredentialsException extends DomainException {

    public InvalidAgentCredentialsException() {
        super("Invalid agent credentials provided");
    }

    public InvalidAgentCredentialsException(String reason) {
        super("Invalid agent credentials: " + reason);
    }
}
