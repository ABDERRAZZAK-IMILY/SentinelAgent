package com.sentinelagent.backend.telemetry.internal.messaging;

import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import lombok.Data;

@Data
public class TelemetryKafkaMessage {
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
    private TelemetryData.ProcessData[] processes;
    private TelemetryData.NetworkConnectionData[] networkConnections;

    public TelemetryData toTelemetryData() {
        return TelemetryData.builder()
                .agentId(agentId)
                .apiKey(apiKey)
                .hostname(hostname)
                .cpuUsage(cpuUsage)
                .ramUsedPercent(ramUsedPercent)
                .ramTotalMb(ramTotalMb)
                .diskUsedPercent(diskUsedPercent)
                .diskTotalGb(diskTotalGb)
                .bytesSentSec(bytesSentSec)
                .bytesRecvSec(bytesRecvSec)
                .processes(processes != null ? java.util.List.of(processes) : null)
                .networkConnections(networkConnections != null ? java.util.List.of(networkConnections) : null)
                .build();
    }
}
