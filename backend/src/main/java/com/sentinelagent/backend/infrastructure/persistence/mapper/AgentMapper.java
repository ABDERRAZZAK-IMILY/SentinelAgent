package com.sentinelagent.backend.infrastructure.persistence.mapper;

import com.sentinelagent.backend.domain.agent.Agent;
import com.sentinelagent.backend.domain.agent.AgentId;
import com.sentinelagent.backend.domain.agent.AgentStatus;
import com.sentinelagent.backend.infrastructure.persistence.entity.AgentDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper between Agent domain entity and AgentDocument persistence entity.
 */
@Component
public class AgentMapper {

    /**
     * Convert domain Agent to MongoDB AgentDocument
     */
    public AgentDocument toDocument(Agent agent) {
        if (agent == null)
            return null;

        return AgentDocument.builder()
                .id(agent.getId() != null ? agent.getId().getValue() : null)
                .hostname(agent.getHostname())
                .operatingSystem(agent.getOperatingSystem())
                .agentVersion(agent.getAgentVersion())
                .ipAddress(agent.getIpAddress())
                .status(agent.getStatus() != null ? agent.getStatus().name() : null)
                .registeredAt(agent.getRegisteredAt())
                .lastHeartbeat(agent.getLastHeartbeat())
                .apiKeyHash(agent.getApiKeyHash())
                .build();
    }

    /**
     * Convert MongoDB AgentDocument to domain Agent
     */
    public Agent toDomain(AgentDocument document) {
        if (document == null)
            return null;

        return Agent.builder()
                .id(document.getId() != null ? AgentId.of(document.getId()) : null)
                .hostname(document.getHostname())
                .operatingSystem(document.getOperatingSystem())
                .agentVersion(document.getAgentVersion())
                .ipAddress(document.getIpAddress())
                .status(document.getStatus() != null ? AgentStatus.valueOf(document.getStatus()) : null)
                .registeredAt(document.getRegisteredAt())
                .lastHeartbeat(document.getLastHeartbeat())
                .apiKeyHash(document.getApiKeyHash())
                .build();
    }
}
