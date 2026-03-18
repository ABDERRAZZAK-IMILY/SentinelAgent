package com.sentinelagent.backend.alert.api;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import com.sentinelagent.backend.alert.internal.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@NamedInterface
public class AlertFacade {

    private final AlertService alertService;

    public List<AlertDocument> getAlertsByAgent(String agentId) {
        return alertService.getAlertsByAgent(agentId);
    }

    public List<AlertDocument> getAll(String status, String severity) {
        return alertService.getAlerts(status, severity);
    }

    public Optional<AlertDocument> getById(String id) {
        return alertService.getById(id);
    }

    public AlertDocument updateStatus(String id, String status) {
        return alertService.updateStatus(id, status);
    }

    public AlertDocument save(AlertDocument alert) {
        return alertService.save(alert);
    }
}
