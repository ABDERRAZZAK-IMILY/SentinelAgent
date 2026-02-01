package com.sentinelagent.backend.domain.agent;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Agent Entity - Core domain object representing a registered security agent.
 * Part of the Domain Layer - no external dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    private AgentId id;
    private String hostname;
    private String operatingSystem;
    private String agentVersion;
    private String ipAddress;
    private AgentStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;
    private String apiKeyHash; // BCrypt hashed API key - never store plain text

    /**
     * Check if the agent is considered stale (no heartbeat for specified duration)
     */
    public boolean isStale(int thresholdMinutes) {
        if (lastHeartbeat == null)
            return true;
        return LocalDateTime.now().minusMinutes(thresholdMinutes).isAfter(lastHeartbeat);
    }

    /**
     * Update the heartbeat timestamp
     */
    public void recordHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }

    /**
     * Activate the agent
     */
    public void activate() {
        this.status = AgentStatus.ACTIVE;
    }

    /**
     * Revoke the agent's access
     */
    public void revoke() {
        this.status = AgentStatus.REVOKED;
    }

    /**
     * Mark agent as inactive
     */
    public void markInactive() {
        this.status = AgentStatus.INACTIVE;
    }
}
