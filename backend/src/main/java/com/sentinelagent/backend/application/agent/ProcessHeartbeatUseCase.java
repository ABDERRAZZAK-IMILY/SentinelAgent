package com.sentinelagent.backend.application.agent;

import com.sentinelagent.backend.application.agent.dto.HeartbeatRequest;
import com.sentinelagent.backend.application.agent.port.ApiKeyService;
import com.sentinelagent.backend.domain.agent.*;
import com.sentinelagent.backend.domain.agent.exception.AgentNotFoundException;
import com.sentinelagent.backend.domain.agent.exception.InvalidAgentCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Process Agent Heartbeat
 * 
 * Handles heartbeat processing:
 * 1. Validate agent API key
 * 2. Update last heartbeat timestamp
 * 3. Update agent status if needed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHeartbeatUseCase {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    public void execute(String apiKey, HeartbeatRequest request) {
        log.debug("Processing heartbeat for agent: {}", request.getAgentId());

        // Find agent by ID
        Agent agent = agentRepository.findById(AgentId.of(request.getAgentId()))
                .orElseThrow(() -> new AgentNotFoundException(request.getAgentId()));

        // Validate API key
        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", request.getAgentId());
            throw new InvalidAgentCredentialsException();
        }

        // Check if agent is revoked
        if (agent.getStatus() == AgentStatus.REVOKED) {
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        // Update heartbeat
        agent.recordHeartbeat();

        // Reactivate if was inactive
        if (agent.getStatus() == AgentStatus.INACTIVE) {
            agent.activate();
            log.info("Agent reactivated: {}", agent.getId().getValue());
        }

        agentRepository.save(agent);
        log.debug("Heartbeat processed for agent: {}", agent.getId().getValue());
    }
}
