package com.sentinelagent.backend.agent.internal.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "agent_commands")
public class AgentCommandDocument {

    @Id
    private String id;

    @Indexed
    private String agentId;

    private String command; // ISOLATE_NETWORK, RESTART_SERVICE, KILL_PROCESS
    private String parameters; // e.g., JSON string of parameters {"pid": 1234}

    private String status; // PENDING, EXECUTED, FAILED
    private String resultMessage; // Output from the agent

    private LocalDateTime issuedAt;
    private LocalDateTime executedAt;
}
