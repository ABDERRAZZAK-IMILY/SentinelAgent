package com.sentinelagent.backend.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful Agent registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentRegistrationResponse {

    private String agentId;
    private String apiKey;
    private String status;
    private String message;
}
