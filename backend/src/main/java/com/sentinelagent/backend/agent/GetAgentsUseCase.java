package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.internal.domain.Agent;
import com.sentinelagent.backend.agent.internal.domain.AgentId;
import com.sentinelagent.backend.agent.internal.domain.AgentRepository;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.AgentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAgentsUseCase {

    private final AgentRepository agentRepository;

    public AgentDetailsDto getById(String agentId) {
        Agent agent = agentRepository.findById(AgentId.of(agentId))
                .orElseThrow(() -> new AgentNotFoundException(agentId));
        return mapToDto(agent);
    }

    public List<AgentDetailsDto> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AgentDetailsDto> getAgentsByStatus(String status) {
        AgentStatus agentStatus = AgentStatus.valueOf(status.toUpperCase());
        return agentRepository.findByStatus(agentStatus).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AgentStatsDto getStats() {
        long activeCount = agentRepository.countByStatus(AgentStatus.ACTIVE);
        long inactiveCount = agentRepository.countByStatus(AgentStatus.INACTIVE);
        long revokedCount = agentRepository.countByStatus(AgentStatus.REVOKED);
        long errorCount = agentRepository.countByStatus(AgentStatus.ERROR);

        return new AgentStatsDto(activeCount, inactiveCount, revokedCount, errorCount);
    }

    private AgentDetailsDto mapToDto(Agent agent) {
        return AgentDetailsDto.builder()
                .agentId(agent.getId().getValue())
                .hostname(agent.getHostname())
                .operatingSystem(agent.getOperatingSystem())
                .agentVersion(agent.getAgentVersion())
                .ipAddress(agent.getIpAddress())
                .status(agent.getStatus().name())
                .registeredAt(agent.getRegisteredAt())
                .lastHeartbeat(agent.getLastHeartbeat())
                .build();
    }

    public record AgentStatsDto(long active, long inactive, long revoked, long error) {
    }
}
