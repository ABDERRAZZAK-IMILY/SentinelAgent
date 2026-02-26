package com.sentinelagent.backend.agent.internal.infrastructure.persistence;

import com.sentinelagent.backend.agent.internal.domain.Agent;
import com.sentinelagent.backend.agent.internal.domain.AgentId;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.infrastructure.persistence.AgentDocument;
import org.springframework.stereotype.Component;


@Component
public class AgentMapper {

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
