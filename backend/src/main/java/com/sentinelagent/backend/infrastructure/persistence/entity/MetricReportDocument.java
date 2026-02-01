package com.sentinelagent.backend.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Document for MetricReport persistence.
 * Maps to the 'agent_reports' collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "agent_reports")
public class MetricReportDocument {

    @Id
    private String id;

    // Agent identification
    @Indexed
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
    private List<ProcessDocument> processes;
    private List<NetworkConnectionDocument> networkConnections;

    @Indexed
    private LocalDateTime receivedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessDocument {
        private int pid;
        private String name;
        private double cpuUsage;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NetworkConnectionDocument {
        private int pid;
        private String localAddress;
        private int localPort;
        private String remoteAddress;
        private int remotePort;
        private String status;
        private String processName;
    }
}
