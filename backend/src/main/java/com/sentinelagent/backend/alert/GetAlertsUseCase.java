package com.sentinelagent.backend.alert;

import com.sentinelagent.backend.alert.internal.infrastructure.persistence.AlertDocument;
import com.sentinelagent.backend.alert.internal.infrastructure.persistence.SpringDataAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAlertsUseCase {
    private final SpringDataAlertRepository alertRepository;

    public List<AlertDocument> getAlertsByAgent(String agentId) {
        return alertRepository.findBySourceAgentId(agentId);
    }
}