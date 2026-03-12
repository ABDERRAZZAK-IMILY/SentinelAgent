package com.sentinelagent.backend.agent.internal.domain;

import lombok.*;
import java.time.LocalDateTime;


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
    private String apiKeyHash;


    public boolean isStale(int thresholdMinutes) {
        if (lastHeartbeat == null)
            return true;
        return LocalDateTime.now().minusMinutes(thresholdMinutes).isAfter(lastHeartbeat);
    }

    public void recordHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }


    public void activate() {
        this.status = AgentStatus.ACTIVE;
    }


    public void revoke() {
        this.status = AgentStatus.REVOKED;
    }

    public void markInactive() {
        this.status = AgentStatus.INACTIVE;
    }
}
