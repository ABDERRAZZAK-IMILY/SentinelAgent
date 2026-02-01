package com.sentinelagent.backend.service;

import com.sentinelagent.backend.application.security.NetworkIntelligenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @deprecated Use
 *             {@link com.sentinelagent.backend.application.security.NetworkIntelligenceUseCase}
 *             instead.
 *             This class delegates to the new use case for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@Service
@RequiredArgsConstructor
public class NetworkIntelligenceService {

    private final NetworkIntelligenceUseCase networkIntelligenceUseCase;

    public boolean isMaliciousIp(String ip) {
        return networkIntelligenceUseCase.isMaliciousIp(ip);
    }

    public String getCountryByIp(String ip) {
        return networkIntelligenceUseCase.getCountryByIp(ip);
    }
}