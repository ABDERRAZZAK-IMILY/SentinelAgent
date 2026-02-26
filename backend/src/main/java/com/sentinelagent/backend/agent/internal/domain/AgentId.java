package com.sentinelagent.backend.agent.internal.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;


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


    public static AgentId of(String value) {
        return new AgentId(value);
    }


    public static AgentId generate() {
        return new AgentId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
