package com.sentinelagent.backend.agent.api;

import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentRepository;
import com.sentinelagent.backend.agent.internal.security.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentValidationService {

    private final SpringDataAgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    public AgentValidationResult validate(String agentId, String apiKey) {
        if (agentId == null || agentId.isBlank()) {
            log.debug("Anonymous telemetry received (no agent ID)");
            return null;
        }

        AgentDocument agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) {
            log.warn("Telemetry from unknown agent: {}", agentId);
            return null;
        }

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", agentId);
            throw new InvalidAgentCredentialsException();
        }

        if (AgentStatus.REVOKED.name().equalsIgnoreCase(agent.getStatus())) {
            log.warn("Telemetry from revoked agent: {}", agentId);
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.setLastHeartbeat(LocalDateTime.now());
        if (AgentStatus.INACTIVE.name().equalsIgnoreCase(agent.getStatus())) {
            agent.setStatus(AgentStatus.ACTIVE.name());
        }
        agentRepository.save(agent);

        log.debug("Agent validated: {}", agentId);
        return new AgentValidationResult(agentId, agent.getHostname(), true);
    }

    public record AgentValidationResult(String agentId, String hostname, boolean authenticated) {
    }
}
