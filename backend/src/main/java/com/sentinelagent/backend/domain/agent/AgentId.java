package com.sentinelagent.backend.domain.agent;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * AgentId Value Object - Immutable identifier for an Agent.
 * Part of the Domain Layer.
 */
@Getter
@EqualsAndHashCode
public class AgentId {

    private final String value;

    private AgentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AgentId cannot be null or empty");
        }
        this.value = value;
    }

    /**
     * Create an AgentId from an existing string value
     */
    public static AgentId of(String value) {
        return new AgentId(value);
    }

    /**
     * Generate a new unique AgentId
     */
    public static AgentId generate() {
        return new AgentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
