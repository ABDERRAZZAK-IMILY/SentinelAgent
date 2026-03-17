package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.AgentNotFoundException;
import com.sentinelagent.backend.agent.api.InvalidAgentCredentialsException;
import com.sentinelagent.backend.agent.dto.AgentCommandResultRequest;
import com.sentinelagent.backend.agent.dto.SendCommandRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentCommandDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentCommandRepository;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentRepository;
import com.sentinelagent.backend.agent.internal.security.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentCommandServiceImpl implements AgentCommandService {

    private final SpringDataAgentRepository agentRepository;
    private final SpringDataAgentCommandRepository commandRepository;
    private final ApiKeyService apiKeyService;

    @Override
    public void issueCommand(String agentId, SendCommandRequest request) {
        AgentDocument agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        AgentCommandDocument doc = AgentCommandDocument.builder()
                .agentId(agent.getId())
                .command(request.getCommand())
                .parameters(request.getParameters() == null ? "{}" : request.getParameters())
                .status("PENDING")
                .issuedAt(LocalDateTime.now())
                .build();

        commandRepository.save(doc);
    }

    @Override
    public List<AgentCommandDocument> getPendingCommands(String agentId, String apiKey) {
        validateAgentCredentials(agentId, apiKey);
        return commandRepository.findByAgentIdAndStatusOrderByIssuedAtAsc(agentId, "PENDING");
    }

    @Override
    public AgentCommandDocument updateCommandResult(String agentId, String commandId, String apiKey, AgentCommandResultRequest request) {
        validateAgentCredentials(agentId, apiKey);

        AgentCommandDocument command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Command not found: " + commandId));

        if (!agentId.equals(command.getAgentId())) {
            throw new InvalidAgentCredentialsException("Command does not belong to this agent");
        }

        String normalizedStatus = normalizeStatus(request.getStatus());
        command.setStatus(normalizedStatus);
        command.setResultMessage(request.getResultMessage());
        command.setExecutedAt(LocalDateTime.now());

        return commandRepository.save(command);
    }

    @Override
    public List<AgentCommandDocument> getCommandHistory(String agentId) {
        return commandRepository.findByAgentIdOrderByIssuedAtDesc(agentId);
    }

    private void validateAgentCredentials(String agentId, String apiKey) {
        AgentDocument agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for command dispatch. agentId={}", agentId);
            throw new InvalidAgentCredentialsException();
        }

        if (AgentStatus.REVOKED.name().equalsIgnoreCase(agent.getStatus())) {
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.setLastHeartbeat(LocalDateTime.now());
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

