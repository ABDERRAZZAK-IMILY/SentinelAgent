package com.sentinelagent.backend.telemetry;

import java.util.List;

public record TelemetryReceivedEvent(
        String reportId,
        String agentId,
        String hostname,
        double cpuUsage,
        double ramUsedPercent,
        long bytesSentSec,
        long bytesRecvSec,
        List<ProcessInfo> processes,
        List<NetworkConnectionInfo> networkConnections) {
    public record ProcessInfo(int pid, String name, double cpuUsage, String username) {
    }

    public record NetworkConnectionInfo(int pid, String processName, String remoteAddress, int remotePort,
            String status) {
    }
}
