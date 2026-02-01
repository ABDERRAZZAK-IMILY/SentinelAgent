package com.sentinelagent.backend.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kafka message format for telemetry data.
 * Part of the Infrastructure Layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryKafkaMessage {

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
    private List<ProcessMessage> processes;

    @JsonProperty("networkConnections")
    private List<NetworkConnectionMessage> networkConnections;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProcessMessage {
        private int pid;
        private String name;
        private double cpu;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NetworkConnectionMessage {
        private int pid;

        @JsonProperty("local_address")
        private String localAddress;

        @JsonProperty("local_port")
        private int localPort;

        @JsonProperty("remote_address")
        private String remoteAddress;

        @JsonProperty("remote_port")
        private int remotePort;

        @JsonProperty("process_name")
        private String processName;

        private String status;
    }
}
