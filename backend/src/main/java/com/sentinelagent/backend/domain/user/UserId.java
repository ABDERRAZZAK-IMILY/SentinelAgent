package com.sentinelagent.backend.domain.user;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object for User identifier.
 * Part of the Domain Layer.
 */
@Value
public class UserId {
    String value;

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String id) {
        return new UserId(id);
    }
}
