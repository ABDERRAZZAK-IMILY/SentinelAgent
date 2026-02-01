package com.sentinelagent.backend.domain.common;

/**
 * Base Domain Exception - Parent class for all domain-level exceptions.
 * Part of the Domain Layer.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
