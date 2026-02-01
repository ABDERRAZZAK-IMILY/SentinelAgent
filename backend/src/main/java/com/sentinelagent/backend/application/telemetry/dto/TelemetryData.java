package com.sentinelagent.backend.application.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for telemetry data received from agents via Kafka.
 * Part of the Application Layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryData {

    private String agentId;
    private String apiKey;
    private String hostname;
    private double cpuUsage;
    private double ramUsedPercent;
    private long ramTotalMb;
    private double diskUsedPercent;
    private long diskTotalGb;
    private long bytesSentSec;
    private long bytesRecvSec;
    private LocalDateTime timestamp;

    // For backward compatibility - using both naming conventions
    private List<ProcessData> processes;
    private List<NetworkConnectionData> networkConnections;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessData {
        private int pid;
        private String name;
        private double cpu;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NetworkConnectionData {
        private int pid;
        private String processName;
        private String localAddress;
        private int localPort;
        private String remoteAddress;
        private int remotePort;
        private String status;
    }
}
