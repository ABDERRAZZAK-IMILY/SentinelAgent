package com.sentinelagent.backend.infrastructure.messaging;

import com.sentinelagent.backend.application.security.AnalyzeSecurityUseCase;
import com.sentinelagent.backend.application.telemetry.SaveTelemetryUseCase;
import com.sentinelagent.backend.application.telemetry.ValidateTelemetryUseCase;
import com.sentinelagent.backend.application.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.domain.agent.Agent;
import com.sentinelagent.backend.domain.agent.exception.InvalidAgentCredentialsException;
import com.sentinelagent.backend.domain.telemetry.MetricReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Main Kafka message consumer (Telemetry Ingestion Pipeline).
 *
 * This component follows Clean Architecture principles by acting as a bridge
 * between the Infrastructure layer (Kafka) and the Application layer (Use Cases).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryKafkaConsumer {

    private final ValidateTelemetryUseCase validateTelemetryUseCase;
    private final SaveTelemetryUseCase saveTelemetryUseCase;
    private final AnalyzeSecurityUseCase analyzeSecurityUseCase;

    @KafkaListener(
            topics = "agent-data",
            groupId = "sentinel-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(TelemetryKafkaMessage message) {

        log.info(" [Kafka] Receiving new data from Agent ID: {}", message.getAgentId());

        try {
            TelemetryData telemetryData = message.toTelemetryData();

            Agent agent = validateTelemetryUseCase.execute(telemetryData);
            log.debug(" Agent identity successfully verified: {}", agent.getHostname());

            MetricReport savedReport = saveTelemetryUseCase.execute(telemetryData);
            log.info(
                    " Report successfully saved. Report ID: {}",
                    savedReport.getId().getValue()
            );

            log.debug(" Starting AI-based security analysis...");
            analyzeSecurityUseCase.execute(savedReport);

        } catch (InvalidAgentCredentialsException ex) {
            log.error(
                    " Security alert: Unauthorized data received from Agent ID: {}. Reason: {}",
                    message.getAgentId(),
                    ex.getMessage()
            );

        } catch (Exception ex) {
            log.error(
                    " Critical error while processing Kafka message: {}",
                    ex.getMessage(),
                    ex
            );
        }
    }
}
