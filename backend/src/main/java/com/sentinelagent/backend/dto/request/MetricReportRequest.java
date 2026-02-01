package com.sentinelagent.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Request DTO for metric reports received from agents via Kafka.
 * Updated to include agent authentication fields.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricReportRequest {

    // Agent authentication fields
    @JsonProperty("agentId")
    private String agentId;

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("hostname")
    private String hostname;

    // System metrics
    @JsonProperty("cpuUsage")
    private double cpuUsage;

    @JsonProperty("ramUsedPercent")
    private double ramUsedPercent;

    @JsonProperty("ram_total_mb")
    private long ramTotalMb;

    @JsonProperty("disk_used_percent")
    private double diskUsedPercent;

    @JsonProperty("disk_total_gb")
    private long diskTotalGb;

    // Network speed metrics
    @JsonProperty("bytesSentSec")
    private long bytesSentSec;

    @JsonProperty("bytesRecvSec")
    private long bytesRecvSec;

    // Process and network details
    private List<ProcessRequest> processes;

    @JsonProperty("networkConnections")
    private List<NetworkConnectionRequest> networkConnections;
}
