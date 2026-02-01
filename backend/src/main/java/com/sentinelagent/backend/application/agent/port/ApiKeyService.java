package com.sentinelagent.backend.application.agent.port;

/**
 * Port interface for API Key generation and validation.
 * Implemented by Infrastructure layer.
 */
public interface ApiKeyService {

    /**
     * Generate a new secure API key
     * 
     * @return Plain text API key (store the hash, return this only once)
     */
    String generateApiKey();

    /**
     * Hash an API key for secure storage
     * 
     * @param plainApiKey The plain text API key
     * @return BCrypt hashed version of the key
     */
    String hashApiKey(String plainApiKey);

    /**
     * Validate if a plain API key matches a stored hash
     * 
     * @param plainApiKey The plain text API key to validate
     * @param storedHash  The stored BCrypt hash
     * @return true if the key matches
     */
    boolean validateApiKey(String plainApiKey, String storedHash);
}
