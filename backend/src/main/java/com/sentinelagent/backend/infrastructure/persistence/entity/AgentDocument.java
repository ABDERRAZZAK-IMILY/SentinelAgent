package com.sentinelagent.backend.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB Document for Agent persistence.
 * Maps to the 'agents' collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "agents")
public class AgentDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String hostname;

    private String operatingSystem;
    private String agentVersion;
    private String ipAddress;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;

    @Indexed
    private String apiKeyHash;
}
