package com.sentinelagent.backend.telemetry.dto;

public record TelemetryAiSummaryResponse(
        String agentId,
        int sampleCount,
        double avgCpuUsage,
        double avgRamUsedPercent,
        double avgUploadMbps,
        double avgDownloadMbps,
        double latestCpuUsage,
        double latestRamUsedPercent,
        String cpuTrend,
        String ramTrend
) {
}
