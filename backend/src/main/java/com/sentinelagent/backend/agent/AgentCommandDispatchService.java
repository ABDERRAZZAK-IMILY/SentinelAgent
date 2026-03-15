package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.internal.domain.Agent;
import com.sentinelagent.backend.agent.internal.domain.AgentId;
import com.sentinelagent.backend.agent.internal.domain.AgentRepository;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.infrastructure.persistence.AgentCommandDocument;
import com.sentinelagent.backend.agent.internal.infrastructure.persistence.SpringDataAgentCommandRepository;
import com.sentinelagent.backend.agent.internal.port.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentCommandDispatchService {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;
    private final SpringDataAgentCommandRepository commandRepository;

    public List<AgentCommandDocument> getPendingCommands(String agentId, String apiKey) {
        validateAgentCredentials(agentId, apiKey);
        return commandRepository.findByAgentIdAndStatusOrderByIssuedAtAsc(agentId, "PENDING");
    }

    public AgentCommandDocument updateCommandResult(
            String agentId,
            String commandId,
            String apiKey,
            String status,
            String resultMessage) {

        validateAgentCredentials(agentId, apiKey);

        AgentCommandDocument command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Command not found: " + commandId));

        if (!agentId.equals(command.getAgentId())) {
            throw new InvalidAgentCredentialsException("Command does not belong to this agent");
        }

        String normalizedStatus = normalizeStatus(status);
        command.setStatus(normalizedStatus);
        command.setResultMessage(resultMessage);
        command.setExecutedAt(LocalDateTime.now());

        return commandRepository.save(command);
    }

    private void validateAgentCredentials(String agentId, String apiKey) {
        Agent agent = agentRepository.findById(AgentId.of(agentId))
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for command dispatch. agentId={}", agentId);
            throw new InvalidAgentCredentialsException();
        }

        if (agent.getStatus() == AgentStatus.REVOKED) {
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.recordHeartbeat();
        agentRepository.save(agent);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "FAILED";
        }
        String normalized = status.trim().toUpperCase();
        if ("SUCCESS".equals(normalized) || "FAILED".equals(normalized)) {
            return normalized;
        }
        return "FAILED";
    }
}
