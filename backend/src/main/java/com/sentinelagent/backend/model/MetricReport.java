package com.sentinelagent.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB document for storing agent telemetry reports.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "agent_reports")
public class MetricReport {

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
    private List<ProcessModel> processes;
    private List<NetworkConnectionModel> networkConnections;

    @Indexed
    private LocalDateTime receivedAt;
}
