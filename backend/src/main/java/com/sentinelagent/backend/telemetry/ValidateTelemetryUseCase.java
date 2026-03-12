package com.sentinelagent.backend.telemetry;

import com.sentinelagent.backend.agent.AgentValidationService;
import com.sentinelagent.backend.agent.InvalidAgentCredentialsException;
import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateTelemetryUseCase {

    private final AgentValidationService agentValidationService;

    public AgentValidationService.AgentValidationResult execute(TelemetryData telemetry) {
        return agentValidationService.validate(telemetry.getAgentId(), telemetry.getApiKey());
    }
}
