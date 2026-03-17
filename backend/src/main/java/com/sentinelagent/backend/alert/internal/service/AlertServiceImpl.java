package com.sentinelagent.backend.alert.internal.service;

import com.sentinelagent.backend.alert.internal.domain.AlertDocument;
import com.sentinelagent.backend.alert.internal.repository.SpringDataAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final SpringDataAlertRepository alertRepository;

    @Override
    public List<AlertDocument> getAlerts(String status, String severity) {
        if (status != null && !status.isBlank()) {
            return alertRepository.findByStatus(status.toUpperCase());
        }
        if (severity != null && !severity.isBlank()) {
            return alertRepository.findBySeverity(severity.toUpperCase());
        }
        return alertRepository.findAll();
    }

    @Override
    public List<AlertDocument> getAlertsByAgent(String agentId) {
        return alertRepository.findBySourceAgentId(agentId);
    }

    @Override
    public Optional<AlertDocument> getById(String id) {
        return alertRepository.findById(id);
    }

    @Override
    public AlertDocument updateStatus(String id, String status) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setStatus(status.toUpperCase());
                    return alertRepository.save(alert);
                })
                .orElse(null);
    }

    @Override
    public AlertDocument save(AlertDocument alert) {
        return alertRepository.save(alert);
    }
}

