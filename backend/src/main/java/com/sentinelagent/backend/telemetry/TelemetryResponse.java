package com.sentinelagent.backend.telemetry;

import java.time.LocalDateTime;
import java.util.List;

public record TelemetryResponse(
        String agentId,
        String hostname,
        double cpuUsage,
        double ramUsedPercent,
        long ramTotalMb,
        double diskUsedPercent,
        long diskTotalGb,
        long bytesSentSec,
        long bytesRecvSec,
        List<ProcessResponse> processes,
        List<NetworkConnectionResponse> networkConnections,
        LocalDateTime receivedAt
) {
    public record ProcessResponse(int pid, String name, double cpuUsage, String username) {}

    public record NetworkConnectionResponse(int pid, String localAddress, int localPort,
                                            String remoteAddress, int remotePort,
                                            String status, String processName) {}
}

