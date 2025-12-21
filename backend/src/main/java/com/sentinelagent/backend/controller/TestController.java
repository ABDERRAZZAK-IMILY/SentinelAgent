package com.sentinelagent.backend.controller;

import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.model.ProcessModel;
import com.sentinelagent.backend.service.AISecurityAnalyst;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final AISecurityAnalyst aiAnalyst;

    public TestController(AISecurityAnalyst aiAnalyst) {
        this.aiAnalyst = aiAnalyst;
    }

    @PostMapping("/simulate-attack")
    public String simulateAttack() {
        MetricReport fakeReport = new MetricReport();
        fakeReport.setCpuUsage(98.5);
        fakeReport.setRamUsedPercent(85.0);

        ProcessModel maliciousProcess = new ProcessModel();
        maliciousProcess.setName("wannacry.exe");
        maliciousProcess.setPid(666);
        fakeReport.setProcesses(List.of(maliciousProcess));

        String analysis = aiAnalyst.analyzeRisk(fakeReport);

        return analysis;
    }
}