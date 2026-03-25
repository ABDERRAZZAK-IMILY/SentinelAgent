package com.sentinelagent.backend.securityanalysis.internal.controller;

import com.sentinelagent.backend.securityanalysis.event.SecurityAlertGeneratedEvent;
import com.sentinelagent.backend.securityanalysis.internal.domain.AnalysisResult;
import com.sentinelagent.backend.securityanalysis.internal.service.SecurityAnalysisService;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SecurityAnalysisService analysisService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/simulate-attack")
    @Transactional
    public ResponseEntity<AnalysisResult> simulateAttack() {
        log.info("[TestController] simulate-attack triggered");
        TelemetryReceivedEvent fakeEvent = new TelemetryReceivedEvent(
                "test-report-001",
                "62cc714f-6b43-46cd-b8d1-7ad52a8d6007",
                "hicham",
                99.5,
                80.0,
                5_000_000L,
                1_000_000L,
                List.of(new TelemetryReceivedEvent.ProcessInfo(6667, "hollowKignt.exe", 70.5, "root")),
                List.of());

        AnalysisResult result = analysisService.analyzeTelemetry(fakeEvent);
        String severity = result.riskLevel();
        String threatType = result.threatType();
        log.info("[TestController] AI result risk={} threat={} desc={} rec={}", severity, threatType, result.description(), result.recommendation());

        if (!"SAFE".equalsIgnoreCase(severity) && !"LOW".equalsIgnoreCase(severity)) {
            SecurityAlertGeneratedEvent alertEvent = new SecurityAlertGeneratedEvent(
                    fakeEvent.agentId(),
                    severity,
                    threatType,
                    result.description(),
                    result.recommendation(),
                    LocalDateTime.now()
            );
            log.info("[TestController] Publishing SecurityAlertGeneratedEvent for agent={}", fakeEvent.agentId());
            eventPublisher.publishEvent(alertEvent);
        } else {
            log.info("[TestController] No alert published because risk is {}", severity);
        }

        return ResponseEntity.ok(result);
    }
}