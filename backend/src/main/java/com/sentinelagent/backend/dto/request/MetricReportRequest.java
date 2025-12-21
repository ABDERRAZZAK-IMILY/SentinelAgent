package com.sentinelagent.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricReportRequest {

    private double cpu;

    @JsonProperty("ram_used_percent")
    private double ramUsedPercent;

    @JsonProperty("ram_total_mb")
    private long ramTotalMb;

    @JsonProperty("disk_used_percent")
    private double diskUsedPercent;

    @JsonProperty("disk_total_gb")
    private long diskTotalGb;

    private List<ProcessRequest> processes;

    @JsonProperty("network_connections")
    private List<NetworkConnectionRequest> networkConnections;
}