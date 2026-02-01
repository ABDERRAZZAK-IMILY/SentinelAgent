package com.sentinelagent.backend.application.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Agent heartbeat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeartbeatRequest {

    @NotBlank(message = "Agent ID is required")
    private String agentId;

    private double cpuUsage;
    private double ramUsedPercent;
    private String status;
}
