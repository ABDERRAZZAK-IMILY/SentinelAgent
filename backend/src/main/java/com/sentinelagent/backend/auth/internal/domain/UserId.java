package com.sentinelagent.backend.auth.internal.domain;

import lombok.Value;

import java.util.UUID;

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
