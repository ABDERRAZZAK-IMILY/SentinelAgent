package com.sentinelagent.backend.agent.internal.domain;

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

    private String command;
    private String parameters;

    private String status;
    private String resultMessage;

    private LocalDateTime issuedAt;
    private LocalDateTime executedAt;
}
