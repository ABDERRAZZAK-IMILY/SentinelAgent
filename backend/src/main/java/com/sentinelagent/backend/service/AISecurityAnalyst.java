package com.sentinelagent.backend.service;

import com.sentinelagent.backend.application.security.AnalyzeSecurityUseCase;
import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.domain.telemetry.NetworkConnection;
import com.sentinelagent.backend.domain.telemetry.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated Use
 *             {@link com.sentinelagent.backend.application.security.AnalyzeSecurityUseCase}
 *             instead.
 *             This class delegates to the new use case for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@Service
@RequiredArgsConstructor
public class AISecurityAnalyst {

    private final AnalyzeSecurityUseCase analyzeSecurityUseCase;

    public String analyzeRisk(MetricReport report) {
        // Convert old model to domain entity
        com.sentinelagent.backend.domain.telemetry.MetricReport domainReport = com.sentinelagent.backend.domain.telemetry.MetricReport
                .builder()
                .agentId(report.getAgentId())
                .hostname(report.getHostname())
                .cpuUsage(report.getCpuUsage())
                .ramUsedPercent(report.getRamUsedPercent())
                .ramTotalMb(report.getRamTotalMb())
                .diskUsedPercent(report.getDiskUsedPercent())
                .diskTotalGb(report.getDiskTotalGb())
                .bytesSentSec(report.getBytesSentSec())
                .bytesRecvSec(report.getBytesRecvSec())
                .processes(mapProcesses(report.getProcesses()))
                .networkConnections(mapConnections(report.getNetworkConnections()))
                .receivedAt(report.getReceivedAt())
                .build();

        return analyzeSecurityUseCase.execute(domainReport);
    }

    private List<Process> mapProcesses(List<com.sentinelagent.backend.model.ProcessModel> processes) {
        if (processes == null)
            return List.of();
        return processes.stream()
                .map(p -> Process.builder()
                        .pid(p.getPid())
                        .name(p.getName())
                        .cpuUsage(p.getCpuUsage())
                        .username(p.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<NetworkConnection> mapConnections(
            List<com.sentinelagent.backend.model.NetworkConnectionModel> connections) {
        if (connections == null)
            return List.of();
        return connections.stream()
                .map(c -> NetworkConnection.builder()
                        .pid(c.getPid())
                        .localAddress(c.getLocalAddress())
                        .localPort(c.getLocalPort())
                        .remoteAddress(c.getRemoteAddress())
                        .remotePort(c.getRemotePort())
                        .status(c.getStatus())
                        .processName(c.getProcessName())
                        .build())
                .collect(Collectors.toList());
    }
}