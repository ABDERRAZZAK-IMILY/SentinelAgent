package com.sentinelagent.backend.controller;

import com.sentinelagent.backend.application.security.AnalyzeSecurityUseCase;
import com.sentinelagent.backend.domain.telemetry.MetricReport;
import com.sentinelagent.backend.domain.telemetry.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @deprecated Use {@link com.sentinelagent.backend.api.v1.test.TestController}
 *             instead.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/api/test-legacy")
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