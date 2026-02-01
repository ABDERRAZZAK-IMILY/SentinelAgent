package com.sentinelagent.backend.application.agent.dto;

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
    private String apiKey; // Plain text - only returned once during registration!
    private String status;
    private String message;
}
