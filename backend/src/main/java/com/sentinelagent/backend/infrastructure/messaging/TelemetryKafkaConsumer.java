package com.sentinelagent.backend.infrastructure.messaging;

import com.sentinelagent.backend.application.telemetry.ValidateTelemetryUseCase;
import com.sentinelagent.backend.application.telemetry.SaveTelemetryUseCase;
import com.sentinelagent.backend.application.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.domain.agent.Agent;
import com.sentinelagent.backend.domain.agent.exception.InvalidAgentCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kafka Consumer for processing agent telemetry data.
 * Part of the Infrastructure Layer.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryKafkaConsumer {

    private final ValidateTelemetryUseCase validateTelemetryUseCase;
    private final SaveTelemetryUseCase saveTelemetryUseCase;

    @KafkaListener(topics = "agent-data", groupId = "sentinel-consumer-group")
    public void consume(TelemetryKafkaMessage message) {
        log.info("📥 Received telemetry from Kafka");

        try {
            // Convert to telemetry data for validation
            TelemetryData telemetryData = TelemetryData.builder()
                    .agentId(message.getAgentId())
                    .apiKey(message.getApiKey())
                    .hostname(message.getHostname())
                    .cpuUsage(message.getCpuUsage())
                    .ramUsedPercent(message.getRamUsedPercent())
                    .ramTotalMb(message.getRamTotalMb())
                    .diskUsedPercent(message.getDiskUsedPercent())
                    .diskTotalGb(message.getDiskTotalGb())
                    .bytesSentSec(message.getBytesSentSec())
                    .bytesRecvSec(message.getBytesRecvSec())
                    .processes(mapProcesses(message.getProcesses()))
                    .networkConnections(mapConnections(message.getNetworkConnections()))
                    .build();

            // Validate agent credentials
            Agent agent = validateTelemetryUseCase.execute(telemetryData);

            if (agent != null) {
                log.info("✅ Telemetry from registered agent: {} ({})",
                        agent.getHostname(), agent.getId().getValue());
            } else {
                log.info("📡 Anonymous telemetry received (no agent registration)");
            }

            // Save telemetry
            var savedReport = saveTelemetryUseCase.execute(telemetryData);
            log.info("💾 Saved report to DB with ID: {}", savedReport.getId().getValue());

        } catch (InvalidAgentCredentialsException ex) {
            log.warn("🚫 Rejected telemetry from unauthorized agent: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("❌ Error processing telemetry: {}", ex.getMessage(), ex);
        }
    }

    private List<TelemetryData.ProcessData> mapProcesses(List<TelemetryKafkaMessage.ProcessMessage> processes) {
        if (processes == null)
            return List.of();
        return processes.stream()
                .map(p -> TelemetryData.ProcessData.builder()
                        .pid(p.getPid())
                        .name(p.getName())
                        .cpu(p.getCpu())
                        .username(p.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TelemetryData.NetworkConnectionData> mapConnections(
            List<TelemetryKafkaMessage.NetworkConnectionMessage> connections) {
        if (connections == null)
            return List.of();
        return connections.stream()
                .map(c -> TelemetryData.NetworkConnectionData.builder()
                        .pid(c.getPid())
                        .localAddress(c.getLocalAddress())
                        .localPort(c.getLocalPort())
                        .remoteAddress(c.getRemoteAddress())
                        .remotePort(c.getRemotePort())
                        .status(c.getStatus())
                        .processName(c.getProcessName())
                        .build())
                .collect(Collectors.toList());
    }
}
