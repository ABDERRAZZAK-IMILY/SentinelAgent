package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.securityanalysis.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final AnalyzeSecurityUseCase analyzeSecurityUseCase;

    @PostMapping("/simulate-attack")
    public ResponseEntity<AnalysisResult> simulateAttack() {
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

        return ResponseEntity.ok(result);
    }
}