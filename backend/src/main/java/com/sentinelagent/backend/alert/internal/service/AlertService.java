package com.sentinelagent.backend.alert.internal.service;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;

import java.util.List;
import java.util.Optional;

public interface AlertService {
    List<AlertDocument> getAlerts(String status, String severity);
    List<AlertDocument> getAlertsByAgent(String agentId);
    Optional<AlertDocument> getById(String id);
    AlertDocument updateStatus(String id, String status);
    AlertDocument save(AlertDocument alert);
}

