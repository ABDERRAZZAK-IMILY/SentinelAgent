package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.dto.AgentRegistrationRequest;
import com.sentinelagent.backend.agent.dto.AgentRegistrationResponse;
import com.sentinelagent.backend.agent.dto.HeartbeatRequest;

import java.util.List;

public interface AgentService {
    AgentRegistrationResponse registerAgent(AgentRegistrationRequest request);
    void processHeartbeat(String apiKey, HeartbeatRequest request);
    AgentDetailsDto getById(String agentId);
    List<AgentDetailsDto> getAllAgents();
    List<AgentDetailsDto> getAgentsByStatus(String status);
    AgentStatsDto getStats();

    record AgentStatsDto(long active, long inactive, long revoked, long error) {}
}

