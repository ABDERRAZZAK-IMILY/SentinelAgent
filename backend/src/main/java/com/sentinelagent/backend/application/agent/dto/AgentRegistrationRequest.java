package com.sentinelagent.backend.application.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Agent registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentRegistrationRequest {

    @NotBlank(message = "Hostname is required")
    private String hostname;

    @NotBlank(message = "Operating system is required")
    private String operatingSystem;

    @NotBlank(message = "Agent version is required")
    private String agentVersion;

    private String ipAddress;
}
