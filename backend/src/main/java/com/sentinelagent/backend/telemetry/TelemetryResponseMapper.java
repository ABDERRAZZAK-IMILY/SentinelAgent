package com.sentinelagent.backend.telemetry;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TelemetryResponseMapper {

    public TelemetryResponse toResponse(MetricReport report) {
        return new TelemetryResponse(
                report.getAgentId(),
                report.getHostname(),
                report.getCpuUsage(),
                report.getRamUsedPercent(),
                report.getRamTotalMb(),
                report.getDiskUsedPercent(),
                report.getDiskTotalGb(),
                report.getBytesSentSec(),
                report.getBytesRecvSec(),
                mapProcesses(report.getProcesses()),
                mapConnections(report.getNetworkConnections()),
                report.getReceivedAt()
        );
    }

    private List<TelemetryResponse.ProcessResponse> mapProcesses(List<Process> processes) {
        if (processes == null) {
            return Collections.emptyList();
        }
        return processes.stream()
                .map(p -> new TelemetryResponse.ProcessResponse(
                        p.getPid(),
                        p.getName(),
                        p.getCpuUsage(),
                        p.getUsername()))
                .collect(Collectors.toList());
    }

    private List<TelemetryResponse.NetworkConnectionResponse> mapConnections(List<NetworkConnection> connections) {
        if (connections == null) {
            return Collections.emptyList();
        }
        return connections.stream()
                .map(c -> new TelemetryResponse.NetworkConnectionResponse(
                        c.getPid(),
                        c.getLocalAddress(),
                        c.getLocalPort(),
                        c.getRemoteAddress(),
                        c.getRemotePort(),
                        c.getStatus(),
                        c.getProcessName()))
                .collect(Collectors.toList());
    }
}

