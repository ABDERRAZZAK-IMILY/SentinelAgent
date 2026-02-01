package com.sentinelagent.backend.application.agent;

import com.sentinelagent.backend.application.agent.dto.AgentRegistrationRequest;
import com.sentinelagent.backend.application.agent.dto.AgentRegistrationResponse;
import com.sentinelagent.backend.application.agent.port.ApiKeyService;
import com.sentinelagent.backend.domain.agent.*;
import com.sentinelagent.backend.domain.agent.exception.AgentAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Use Case: Register a new Agent
 * 
 * Handles the registration flow:
 * 1. Check for duplicate registration (same hostname)
 * 2. Generate secure API key
 * 3. Create and persist Agent entity
 * 4. Return registration response with API key (only shown once)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterAgentUseCase {

    private final AgentRepository agentRepository;
    private final ApiKeyService apiKeyService;

    public AgentRegistrationResponse execute(AgentRegistrationRequest request) {
        log.info("Registering new agent: hostname={}", request.getHostname());

        // Check for duplicate registration
        if (agentRepository.existsByHostname(request.getHostname())) {
            throw new AgentAlreadyExistsException(request.getHostname());
        }

        // Generate secure API key
        String plainApiKey = apiKeyService.generateApiKey();
        String apiKeyHash = apiKeyService.hashApiKey(plainApiKey);

        // Create Agent entity
        Agent agent = Agent.builder()
                .id(AgentId.generate())
                .hostname(request.getHostname())
                .operatingSystem(request.getOperatingSystem())
                .agentVersion(request.getAgentVersion())
                .ipAddress(request.getIpAddress())
                .status(AgentStatus.ACTIVE)
                .registeredAt(LocalDateTime.now())
                .lastHeartbeat(LocalDateTime.now())
                .apiKeyHash(apiKeyHash)
                .build();

        // Persist agent
        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent registered successfully: id={}, hostname={}",
                savedAgent.getId().getValue(), savedAgent.getHostname());

        // Return response with plain API key (shown only once!)
        return AgentRegistrationResponse.builder()
                .agentId(savedAgent.getId().getValue())
                .apiKey(plainApiKey)
                .status(savedAgent.getStatus().name())
                .message("Agent registered successfully. Store the API key securely - it will not be shown again.")
                .build();
    }
}
