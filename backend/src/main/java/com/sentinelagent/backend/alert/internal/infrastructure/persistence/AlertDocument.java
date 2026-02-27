package com.sentinelagent.backend.alert.internal.infrastructure.persistence;

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
@Document(collection = "alerts")
public class AlertDocument {

    @Id
    private String id;

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String threatType;
    private String description;
    private String recommendation;

    @Indexed
    private String sourceAgentId;

    private String status; // NEW, REVIEWED, RESOLVED
    private LocalDateTime timestamp;
}
