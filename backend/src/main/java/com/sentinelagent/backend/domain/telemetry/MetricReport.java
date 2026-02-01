package com.sentinelagent.backend.domain.telemetry;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain Entity representing agent telemetry reports.
 * Part of the Domain Layer - no external dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricReport {

    private MetricReportId id;

    // Agent identification
    private String agentId;
    private String hostname;

    // System metrics
    private double cpuUsage;
    private double ramUsedPercent;
    private long ramTotalMb;
    private double diskUsedPercent;
    private long diskTotalGb;

    // Network speed
    private long bytesSentSec;
    private long bytesRecvSec;

    // Details
    private List<Process> processes;
    private List<NetworkConnection> networkConnections;

    private LocalDateTime receivedAt;

    /**
     * Check if CPU usage is critically high
     */
    public boolean isCpuCritical(double threshold) {
        return cpuUsage >= threshold;
    }

    /**
     * Check if RAM usage is critically high
     */
    public boolean isRamCritical(double threshold) {
        return ramUsedPercent >= threshold;
    }

    /**
     * Get upload speed in MB/s
     */
    public double getUploadSpeedMbps() {
        return bytesSentSec / 1024.0 / 1024.0;
    }

    /**
     * Get download speed in MB/s
     */
    public double getDownloadSpeedMbps() {
        return bytesRecvSec / 1024.0 / 1024.0;
    }
}
