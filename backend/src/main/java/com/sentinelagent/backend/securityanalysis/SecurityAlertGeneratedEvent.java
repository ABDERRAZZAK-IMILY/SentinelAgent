package com.sentinelagent.backend.securityanalysis;

import java.time.LocalDateTime;

public record SecurityAlertGeneratedEvent(
        String agentId,
        String severity,
        String threatType,
        String description,
        String recommendation,
        LocalDateTime timestamp) {
}
