package com.sentinelagent.backend.agent.internal.mapper;

import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import org.springframework.stereotype.Component;


@Component
public class AgentMapper {
    public AgentDetailsDto toDetailsDto(AgentDocument document) {
        if (document == null) {
            return null;
        }
        return AgentDetailsDto.builder()
                .agentId(document.getId())
                .hostname(document.getHostname())
                .operatingSystem(document.getOperatingSystem())
                .agentVersion(document.getAgentVersion())
                .ipAddress(document.getIpAddress())
                .status(document.getStatus())
                .registeredAt(document.getRegisteredAt())
                .lastHeartbeat(document.getLastHeartbeat())
                .build();
    }
}
