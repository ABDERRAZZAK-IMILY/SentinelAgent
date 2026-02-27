package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.securityanalysis.AnalyzeSecurityUseCase;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.Process;
import lombok.RequiredArgsConstructor;
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
    public String simulateAttack() {
        MetricReport fakeReport = MetricReport.builder()
                .cpuUsage(20.5)
                .ramUsedPercent(20.0)
                .processes(List.of(
                        Process.builder()
                                .name("facebook.exe")
                                .pid(666)
                                .build()))
                .build();

        return analyzeSecurityUseCase.execute(fakeReport);
    }
}
