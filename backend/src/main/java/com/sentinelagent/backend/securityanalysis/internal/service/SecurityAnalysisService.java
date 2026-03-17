package com.sentinelagent.backend.securityanalysis.internal.service;

import com.sentinelagent.backend.securityanalysis.internal.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.TelemetryReceivedEvent;

public interface SecurityAnalysisService {
    AnalysisResult analyzeReport(MetricReport report);
    AnalysisResult analyzeTelemetry(TelemetryReceivedEvent event);
}

