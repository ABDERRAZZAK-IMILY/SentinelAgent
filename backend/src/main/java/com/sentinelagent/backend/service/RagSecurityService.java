package com.sentinelagent.backend.service;

import com.sentinelagent.backend.application.security.RagSecurityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @deprecated Use
 *             {@link com.sentinelagent.backend.application.security.RagSecurityUseCase}
 *             instead.
 *             This class delegates to the new use case for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@Service
@RequiredArgsConstructor
public class RagSecurityService {

    private final RagSecurityUseCase ragSecurityUseCase;

    public String findMitigationStrategy(String threatDescription) {
        return ragSecurityUseCase.findMitigationStrategy(threatDescription);
    }
}