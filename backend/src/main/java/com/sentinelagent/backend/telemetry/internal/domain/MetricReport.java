package com.sentinelagent.backend.telemetry.internal.domain;

import com.sentinelagent.backend.telemetry.internal.domain.MetricReportId;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


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


    public boolean isCpuCritical(double threshold) {
        return cpuUsage >= threshold;
    }


    public boolean isRamCritical(double threshold) {
        return ramUsedPercent >= threshold;
    }

    public double getUploadSpeedMbps() {
        return bytesSentSec / 1024.0 / 1024.0;
    }


    public double getDownloadSpeedMbps() {
        return bytesRecvSec / 1024.0 / 1024.0;
    }
}
