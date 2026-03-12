package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.dto.HeartbeatRequest;
import com.sentinelagent.backend.agent.internal.port.ApiKeyService;
import com.sentinelagent.backend.agent.internal.domain.*;
import com.sentinelagent.backend.agent.AgentNotFoundException;
import com.sentinelagent.backend.agent.InvalidAgentCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHeartbeatUseCase {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    public void execute(String apiKey, HeartbeatRequest request) {
        log.debug("Processing heartbeat for agent: {}", request.getAgentId());

        Agent agent = agentRepository.findById(AgentId.of(request.getAgentId()))
                .orElseThrow(() -> new AgentNotFoundException(request.getAgentId()));

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", request.getAgentId());
            throw new InvalidAgentCredentialsException();
        }

        if (agent.getStatus() == AgentStatus.REVOKED) {
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.recordHeartbeat();

        if (agent.getStatus() == AgentStatus.INACTIVE) {
            agent.activate();
            log.info("Agent reactivated: {}", agent.getId().getValue());
        }

        agentRepository.save(agent);
        log.debug("Heartbeat processed for agent: {}", agent.getId().getValue());
    }
}
