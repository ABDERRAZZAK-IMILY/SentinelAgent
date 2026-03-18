package com.sentinelagent.backend.telemetry.internal.messaging;

import com.sentinelagent.backend.telemetry.internal.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryKafkaConsumer {

    private final TelemetryService telemetryService;

    @KafkaListener(topics = "agent-data", groupId = "sentinel-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(TelemetryKafkaMessage message) {
        log.info(" [Kafka] Receiving new data from Agent ID: {}", message.getAgentId());
        try {
            telemetryService.processAndSaveTelemetry(message.toTelemetryData());
        } catch (Exception ex) {
            log.error(" Error processing Kafka message for Agent: {}", message.getAgentId(), ex);
        }
    }
}
