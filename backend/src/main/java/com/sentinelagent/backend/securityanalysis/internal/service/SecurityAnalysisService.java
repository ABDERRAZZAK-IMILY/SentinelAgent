package com.sentinelagent.backend.securityanalysis.internal.service;

import com.sentinelagent.backend.securityanalysis.internal.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;

public interface SecurityAnalysisService {
    AnalysisResult analyzeTelemetry(TelemetryReceivedEvent event);
}
