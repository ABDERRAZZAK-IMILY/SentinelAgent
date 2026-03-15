package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.securityanalysis.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.Process;
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

    private final AnalyzeSecurityUseCase analyzeSecurityUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/simulate-attack")
    @Transactional
    public ResponseEntity<AnalysisResult> simulateAttack() {
        log.info("[TestController] simulate-attack triggered");
        MetricReport fakeReport = MetricReport.builder()
                .agentId("test-agent-001")
                .hostname("Testing-PC")
                .cpuUsage(95.5)
                .ramUsedPercent(80.0)
                .bytesSentSec(5000000L)
                .processes(List.of(
                        Process.builder()
                                .name("suspicious_miner.exe")
                                .pid(666)
                                .cpuUsage(70.5)
                                .build()))
                .build();

        AnalysisResult result = analyzeSecurityUseCase.execute(fakeReport);
        String riskLevel = result.riskLevel();
        String threatType = result.threatType();
        log.info("[TestController] AI result risk={} threat={} desc={} rec={}", riskLevel, threatType, result.description(), result.recommendation());

        if (!"SAFE".equalsIgnoreCase(riskLevel) && !"LOW".equalsIgnoreCase(riskLevel)) {
            SecurityAlertGeneratedEvent alertEvent = new SecurityAlertGeneratedEvent(
                    fakeReport.getAgentId(),
                    riskLevel,
                    threatType,
                    result.description(),
                    result.recommendation(),
                    LocalDateTime.now()
            );
            log.info("[TestController] Publishing SecurityAlertGeneratedEvent for agent={}", fakeReport.getAgentId());
            eventPublisher.publishEvent(alertEvent);
        } else {
            log.info("[TestController] No alert published because risk is {}", riskLevel);
        }

        return ResponseEntity.ok(result);
    }
}