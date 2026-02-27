package com.sentinelagent.backend.telemetry.internal.infrastructure.messaging;

import com.sentinelagent.backend.agent.AgentValidationService;
import com.sentinelagent.backend.agent.InvalidAgentCredentialsException;
import com.sentinelagent.backend.telemetry.SaveTelemetryUseCase;
import com.sentinelagent.backend.telemetry.TelemetryReceivedEvent;
import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.telemetry.MetricReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryKafkaConsumer {

    private final AgentValidationService agentValidationService;
    private final SaveTelemetryUseCase saveTelemetryUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @KafkaListener(topics = "agent-data", groupId = "sentinel-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(TelemetryKafkaMessage message) {

        log.info(" [Kafka] Receiving new data from Agent ID: {}", message.getAgentId());

        try {
            TelemetryData telemetryData = message.toTelemetryData();

            // Validate agent via Agent module's public API
            AgentValidationService.AgentValidationResult validationResult = agentValidationService
                    .validate(telemetryData.getAgentId(), telemetryData.getApiKey());
            if (validationResult != null) {
                log.debug(" Agent identity successfully verified: {}", validationResult.hostname());
            }

            // Save telemetry report
            MetricReport savedReport = saveTelemetryUseCase.execute(telemetryData);
            log.info(
                    " Report successfully saved. Report ID: {}",
                    savedReport.getId().getValue());

            // Publish event for Security Analysis module (decoupled communication)
            log.debug(" Publishing TelemetryReceivedEvent for AI security analysis...");
            eventPublisher.publishEvent(new TelemetryReceivedEvent(
                    savedReport.getId().getValue(),
                    savedReport.getAgentId(),
                    savedReport.getHostname(),
                    savedReport.getCpuUsage(),
                    savedReport.getRamUsedPercent(),
                    savedReport.getBytesSentSec(),
                    savedReport.getBytesRecvSec(),
                    savedReport.getProcesses() != null ? savedReport.getProcesses().stream()
                            .map(p -> new TelemetryReceivedEvent.ProcessInfo(
                                    p.getPid(), p.getName(), p.getCpuUsage(), p.getUsername()))
                            .collect(Collectors.toList()) : List.of(),
                    savedReport.getNetworkConnections() != null ? savedReport.getNetworkConnections().stream()
                            .map(n -> new TelemetryReceivedEvent.NetworkConnectionInfo(
                                    n.getPid(), n.getProcessName(), n.getRemoteAddress(),
                                    n.getRemotePort(), n.getStatus()))
                            .collect(Collectors.toList()) : List.of()));

        } catch (InvalidAgentCredentialsException ex) {
            log.error(
                    " Security alert: Unauthorized data received from Agent ID: {}. Reason: {}",
                    message.getAgentId(),
                    ex.getMessage());

        } catch (Exception ex) {
            log.error(
                    " Critical error while processing Kafka message: {}",
                    ex.getMessage(),
                    ex);
        }
    }
}
