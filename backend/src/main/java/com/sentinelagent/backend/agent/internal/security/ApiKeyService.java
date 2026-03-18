package com.sentinelagent.backend.agent.internal.security;

public interface ApiKeyService {
    String generateApiKey();
    String hashApiKey(String plainApiKey);
    boolean validateApiKey(String plainApiKey, String storedHash);
}
