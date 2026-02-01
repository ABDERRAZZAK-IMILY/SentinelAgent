package com.sentinelagent.backend.application.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing Agent details for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDetailsDto {

    private String agentId;
    private String hostname;
    private String operatingSystem;
    private String agentVersion;
    private String ipAddress;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;
}
