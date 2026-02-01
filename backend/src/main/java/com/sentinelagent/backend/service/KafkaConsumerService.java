package com.sentinelagent.backend.service;

import com.sentinelagent.backend.application.telemetry.ValidateTelemetryUseCase;
import com.sentinelagent.backend.application.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.domain.agent.Agent;
import com.sentinelagent.backend.domain.agent.exception.InvalidAgentCredentialsException;
import com.sentinelagent.backend.dto.request.MetricReportRequest;
import com.sentinelagent.backend.mapper.MetricReportMapper;
import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.repository.MetricReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service for processing agent telemetry data.
 * Validates agent credentials before persisting data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final MetricReportRepository repository;
    private final MetricReportMapper mapper;
    private final ValidateTelemetryUseCase validateTelemetryUseCase;

    @KafkaListener(topics = "agent-data", groupId = "sentinel-consumer-group")
    public void consume(MetricReportRequest request) {
        log.info("📥 Received MetricReportRequest from Kafka");

        try {
            // Convert to telemetry data for validation
            TelemetryData telemetryData = TelemetryData.builder()
                    .agentId(request.getAgentId())
                    .apiKey(request.getApiKey())
                    .hostname(request.getHostname())
                    .cpuUsage(request.getCpuUsage())
                    .ramUsedPercent(request.getRamUsedPercent())
                    .build();

            // Validate agent credentials
            Agent agent = validateTelemetryUseCase.execute(telemetryData);

            if (agent != null) {
                log.info("✅ Telemetry from registered agent: {} ({})",
                        agent.getHostname(), agent.getId().getValue());
            } else {
                log.info("📡 Anonymous telemetry received (no agent registration)");
            }

            // Map and save
            MetricReport entity = mapper.toEntity(request);
            MetricReport saved = repository.save(entity);

            log.info("💾 Saved report to DB with ID: {}", saved.getId());

        } catch (InvalidAgentCredentialsException ex) {
            log.warn("🚫 Rejected telemetry from unauthorized agent: {}", ex.getMessage());
            // Don't save data from unauthorized agents
        } catch (Exception ex) {
            log.error("❌ Error processing telemetry: {}", ex.getMessage(), ex);
        }
    }
}
