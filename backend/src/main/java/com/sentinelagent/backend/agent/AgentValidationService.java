package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.internal.domain.*;
import com.sentinelagent.backend.agent.InvalidAgentCredentialsException;
import com.sentinelagent.backend.agent.internal.port.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentValidationService {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    public AgentValidationResult validate(String agentId, String apiKey) {
        if (agentId == null || agentId.isBlank()) {
            log.debug("Anonymous telemetry received (no agent ID)");
            return null;
        }

        Agent agent = agentRepository.findById(AgentId.of(agentId)).orElse(null);

        if (agent == null) {
            log.warn("Telemetry from unknown agent: {}", agentId);
            return null;
        }

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", agentId);
            throw new InvalidAgentCredentialsException();
        }

        if (agent.getStatus() == AgentStatus.REVOKED) {
            log.warn("Telemetry from revoked agent: {}", agentId);
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.recordHeartbeat();
        if (agent.getStatus() == AgentStatus.INACTIVE) {
            agent.activate();
        }
        agentRepository.save(agent);

        log.debug("Agent validated: {}", agentId);
        return new AgentValidationResult(agentId, agent.getHostname(), true);
    }
    public record AgentValidationResult(String agentId, String hostname, boolean authenticated) {
    }
}
