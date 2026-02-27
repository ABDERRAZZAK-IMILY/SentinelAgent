package com.sentinelagent.backend.agent.internal.infrastructure.security;

import com.sentinelagent.backend.agent.internal.port.ApiKeyService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;


@Service
public class SecureApiKeyService implements ApiKeyService {

    private static final int API_KEY_LENGTH = 32; // 256 bits
    private static final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String generateApiKey() {
        byte[] randomBytes = new byte[API_KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);

        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return "snt_" + encoded; // prefix  helper
    }

    @Override
    public String hashApiKey(String plainApiKey) {
        return passwordEncoder.encode(plainApiKey);
    }

    @Override
    public boolean validateApiKey(String plainApiKey, String storedHash) {
        if (plainApiKey == null || storedHash == null) {
            return false;
        }
        return passwordEncoder.matches(plainApiKey, storedHash);
    }
}
