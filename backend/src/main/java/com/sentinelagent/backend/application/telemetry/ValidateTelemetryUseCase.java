package com.sentinelagent.backend.application.telemetry;

import com.sentinelagent.backend.application.agent.port.ApiKeyService;
import com.sentinelagent.backend.application.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.domain.agent.*;
import com.sentinelagent.backend.domain.agent.exception.InvalidAgentCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Validate telemetry data from an agent
 * 
 * Validates that the agent sending data is registered and has valid credentials
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateTelemetryUseCase {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    /**
     * Validate telemetry and return the validated agent
     * 
     * @return The validated Agent if credentials are valid
     * @throws InvalidAgentCredentialsException if validation fails
     */
    public Agent execute(TelemetryData telemetry) {
        String agentId = telemetry.getAgentId();
        String apiKey = telemetry.getApiKey();

        // If no agent ID, allow anonymous telemetry (for backward compatibility)
        if (agentId == null || agentId.isBlank()) {
            log.debug("Anonymous telemetry received (no agent ID)");
            return null;
        }

        // Find agent
        Agent agent = agentRepository.findById(AgentId.of(agentId))
                .orElse(null);

        if (agent == null) {
            log.warn("Telemetry from unknown agent: {}", agentId);
            return null; // Allow but log - could be misconfigured agent
        }

        // Validate API key
        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", agentId);
            throw new InvalidAgentCredentialsException();
        }

        // Check agent status
        if (agent.getStatus() == AgentStatus.REVOKED) {
            log.warn("Telemetry from revoked agent: {}", agentId);
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        // Update heartbeat
        agent.recordHeartbeat();
        if (agent.getStatus() == AgentStatus.INACTIVE) {
            agent.activate();
        }
        agentRepository.save(agent);

        log.debug("Telemetry validated for agent: {}", agentId);
        return agent;
    }
}
