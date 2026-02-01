package com.sentinelagent.backend.infrastructure.persistence.mapper;

import com.sentinelagent.backend.domain.telemetry.*;
import com.sentinelagent.backend.domain.telemetry.Process;
import com.sentinelagent.backend.infrastructure.persistence.entity.MetricReportDocument;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for MetricReport domain entity and MongoDB document.
 */
@Component
public class MetricReportMapper {

    public MetricReportDocument toDocument(MetricReport report) {
        return MetricReportDocument.builder()
                .id(report.getId() != null ? report.getId().getValue() : null)
                .agentId(report.getAgentId())
                .hostname(report.getHostname())
                .cpuUsage(report.getCpuUsage())
                .ramUsedPercent(report.getRamUsedPercent())
                .ramTotalMb(report.getRamTotalMb())
                .diskUsedPercent(report.getDiskUsedPercent())
                .diskTotalGb(report.getDiskTotalGb())
                .bytesSentSec(report.getBytesSentSec())
                .bytesRecvSec(report.getBytesRecvSec())
                .processes(mapProcessesToDocuments(report.getProcesses()))
                .networkConnections(mapConnectionsToDocuments(report.getNetworkConnections()))
                .receivedAt(report.getReceivedAt())
                .build();
    }

    public MetricReport toDomain(MetricReportDocument document) {
        return MetricReport.builder()
                .id(MetricReportId.of(document.getId()))
                .agentId(document.getAgentId())
                .hostname(document.getHostname())
                .cpuUsage(document.getCpuUsage())
                .ramUsedPercent(document.getRamUsedPercent())
                .ramTotalMb(document.getRamTotalMb())
                .diskUsedPercent(document.getDiskUsedPercent())
                .diskTotalGb(document.getDiskTotalGb())
                .bytesSentSec(document.getBytesSentSec())
                .bytesRecvSec(document.getBytesRecvSec())
                .processes(mapDocumentsToProcesses(document.getProcesses()))
                .networkConnections(mapDocumentsToConnections(document.getNetworkConnections()))
                .receivedAt(document.getReceivedAt())
                .build();
    }

    private List<MetricReportDocument.ProcessDocument> mapProcessesToDocuments(List<Process> processes) {
        if (processes == null)
            return Collections.emptyList();
        return processes.stream()
                .map(p -> MetricReportDocument.ProcessDocument.builder()
                        .pid(p.getPid())
                        .name(p.getName())
                        .cpuUsage(p.getCpuUsage())
                        .username(p.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Process> mapDocumentsToProcesses(List<MetricReportDocument.ProcessDocument> docs) {
        if (docs == null)
            return Collections.emptyList();
        return docs.stream()
                .map(d -> Process.builder()
                        .pid(d.getPid())
                        .name(d.getName())
                        .cpuUsage(d.getCpuUsage())
                        .username(d.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<MetricReportDocument.NetworkConnectionDocument> mapConnectionsToDocuments(
            List<NetworkConnection> connections) {
        if (connections == null)
            return Collections.emptyList();
        return connections.stream()
                .map(c -> MetricReportDocument.NetworkConnectionDocument.builder()
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

    private List<NetworkConnection> mapDocumentsToConnections(
            List<MetricReportDocument.NetworkConnectionDocument> docs) {
        if (docs == null)
            return Collections.emptyList();
        return docs.stream()
                .map(d -> NetworkConnection.builder()
                        .pid(d.getPid())
                        .localAddress(d.getLocalAddress())
                        .localPort(d.getLocalPort())
                        .remoteAddress(d.getRemoteAddress())
                        .remotePort(d.getRemotePort())
                        .status(d.getStatus())
                        .processName(d.getProcessName())
                        .build())
                .collect(Collectors.toList());
    }
}
