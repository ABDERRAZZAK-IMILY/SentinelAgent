package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.AgentAlreadyExistsException;
import com.sentinelagent.backend.agent.AgentNotFoundException;
import com.sentinelagent.backend.agent.api.InvalidAgentCredentialsException;
import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.dto.AgentRegistrationRequest;
import com.sentinelagent.backend.agent.dto.AgentRegistrationResponse;
import com.sentinelagent.backend.agent.dto.HeartbeatRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.mapper.AgentMapper;
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
public class AgentServiceImpl implements AgentService {

    private final SpringDataAgentRepository agentRepository;
    private final ApiKeyService apiKeyService;
    private final AgentMapper mapper;

    @Override
    public AgentRegistrationResponse registerAgent(AgentRegistrationRequest request) {
        log.info("Registering new agent: hostname={}", request.getHostname());

        if (agentRepository.existsByHostname(request.getHostname())) {
            throw new AgentAlreadyExistsException(request.getHostname());
        }

        String plainApiKey = apiKeyService.generateApiKey();
        String apiKeyHash = apiKeyService.hashApiKey(plainApiKey);

        AgentDocument document = AgentDocument.builder()
                .hostname(request.getHostname())
                .operatingSystem(request.getOperatingSystem())
                .agentVersion(request.getAgentVersion())
                .ipAddress(request.getIpAddress())
                .status(AgentStatus.ACTIVE.name())
                .registeredAt(LocalDateTime.now())
                .lastHeartbeat(LocalDateTime.now())
                .apiKeyHash(apiKeyHash)
                .build();

        AgentDocument saved = agentRepository.save(document);
        log.info("Agent registered successfully: id={}, hostname={}", saved.getId(), saved.getHostname());

        return AgentRegistrationResponse.builder()
                .agentId(saved.getId())
                .apiKey(plainApiKey)
                .status(saved.getStatus())
                .message("Agent registered successfully. Store the API key securely - it will not be shown again.")
                .build();
    }

    @Override
    public void processHeartbeat(String apiKey, HeartbeatRequest request) {
        log.debug("Processing heartbeat for agent: {}", request.getAgentId());

        AgentDocument agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new AgentNotFoundException(request.getAgentId()));

        if (!apiKeyService.validateApiKey(apiKey, agent.getApiKeyHash())) {
            log.warn("Invalid API key for agent: {}", request.getAgentId());
            throw new InvalidAgentCredentialsException();
        }

        if (AgentStatus.REVOKED.name().equalsIgnoreCase(agent.getStatus())) {
            throw new InvalidAgentCredentialsException("Agent has been revoked");
        }

        agent.setLastHeartbeat(LocalDateTime.now());
        if (AgentStatus.INACTIVE.name().equalsIgnoreCase(agent.getStatus())) {
            agent.setStatus(AgentStatus.ACTIVE.name());
            log.info("Agent reactivated: {}", agent.getId());
        }

        agentRepository.save(agent);
        log.debug("Heartbeat processed for agent: {}", agent.getId());
    }

    @Override
    public AgentDetailsDto getById(String agentId) {
        AgentDocument agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));
        return mapper.toDetailsDto(agent);
    }

    @Override
    public List<AgentDetailsDto> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(mapper::toDetailsDto)
                .toList();
    }

    @Override
    public List<AgentDetailsDto> getAgentsByStatus(String status) {
        AgentStatus agentStatus = AgentStatus.valueOf(status.toUpperCase());
        return agentRepository.findByStatus(agentStatus.name()).stream()
                .map(mapper::toDetailsDto)
                .toList();
    }

    @Override
    public AgentStatsDto getStats() {
        long activeCount = agentRepository.countByStatus(AgentStatus.ACTIVE.name());
        long inactiveCount = agentRepository.countByStatus(AgentStatus.INACTIVE.name());
        long revokedCount = agentRepository.countByStatus(AgentStatus.REVOKED.name());
        long errorCount = agentRepository.countByStatus(AgentStatus.ERROR.name());
        return new AgentStatsDto(activeCount, inactiveCount, revokedCount, errorCount);
    }
}

