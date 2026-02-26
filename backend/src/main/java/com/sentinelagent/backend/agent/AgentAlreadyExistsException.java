package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.shared.exception.DomainException;


public class AgentAlreadyExistsException extends DomainException {

    public AgentAlreadyExistsException(String hostname) {
        super("Agent already registered with hostname: " + hostname);
    }
}
