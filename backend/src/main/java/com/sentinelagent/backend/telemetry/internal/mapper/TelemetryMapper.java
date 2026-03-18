package com.sentinelagent.backend.telemetry.internal.mapper;

import com.sentinelagent.backend.telemetry.dto.TelemetryAiSummaryResponse;
import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.telemetry.dto.TelemetryResponse;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;
import com.sentinelagent.backend.telemetry.internal.domain.MetricReport;
import com.sentinelagent.backend.telemetry.internal.domain.NetworkConnection;
import com.sentinelagent.backend.telemetry.internal.domain.Process;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Component
public class TelemetryMapper {

    public MetricReport toMetricReport(TelemetryData data) {
        return MetricReport.builder()
                .agentId(data.getAgentId())
                .hostname(data.getHostname())
                .cpuUsage(data.getCpuUsage())
                .ramUsedPercent(data.getRamUsedPercent())
                .ramTotalMb(data.getRamTotalMb())
                .diskUsedPercent(data.getDiskUsedPercent())
                .diskTotalGb(data.getDiskTotalGb())
                .bytesSentSec(data.getBytesSentSec())
                .bytesRecvSec(data.getBytesRecvSec())
                .processes(mapProcesses(data.getProcesses()))
                .networkConnections(mapConnections(data.getNetworkConnections()))
                .receivedAt(data.getTimestamp() != null ? data.getTimestamp() : LocalDateTime.now())
                .build();
    }

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
                mapProcessResponses(report.getProcesses()),
                mapConnectionResponses(report.getNetworkConnections()),
                report.getReceivedAt());
    }

    public TelemetryAiSummaryResponse toAiSummary(String agentId, List<MetricReport> reports, LocalDateTime start, LocalDateTime end) {
        if (reports == null || reports.isEmpty()) {
            return new TelemetryAiSummaryResponse(agentId, 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", "UNKNOWN");
        }

        MetricReport first = reports.get(0);
        MetricReport last = reports.get(reports.size() - 1);

        OptionalDouble avgCpu = reports.stream().mapToDouble(MetricReport::getCpuUsage).average();
        OptionalDouble avgRam = reports.stream().mapToDouble(MetricReport::getRamUsedPercent).average();
        OptionalDouble avgUpload = reports.stream().mapToDouble(r -> r.getBytesSentSec() / 1_000_000.0).average();
        OptionalDouble avgDownload = reports.stream().mapToDouble(r -> r.getBytesRecvSec() / 1_000_000.0).average();

        return new TelemetryAiSummaryResponse(
                agentId,
                reports.size(),
                avgCpu.orElse(0),
                avgRam.orElse(0),
                avgUpload.orElse(0),
                avgDownload.orElse(0),
                last.getCpuUsage(),
                last.getRamUsedPercent(),
                toTrend(first.getCpuUsage(), last.getCpuUsage()),
                toTrend(first.getRamUsedPercent(), last.getRamUsedPercent()));
    }

    public TelemetryReceivedEvent toEvent(MetricReport report) {
        return new TelemetryReceivedEvent(
                report.getId() != null ? report.getId().getValue() : null,
                report.getAgentId(),
                report.getHostname(),
                report.getCpuUsage(),
                report.getRamUsedPercent(),
                report.getBytesSentSec(),
                report.getBytesRecvSec(),
                report.getProcesses() != null ? report.getProcesses().stream()
                        .map(p -> new TelemetryReceivedEvent.ProcessInfo(p.getPid(), p.getName(), p.getCpuUsage(), p.getUsername()))
                        .collect(Collectors.toList()) : List.of(),
                report.getNetworkConnections() != null ? report.getNetworkConnections().stream()
                        .map(n -> new TelemetryReceivedEvent.NetworkConnectionInfo(
                                n.getPid(), n.getProcessName(), n.getRemoteAddress(), n.getRemotePort(), n.getStatus()))
                        .collect(Collectors.toList()) : List.of());
    }

    private List<Process> mapProcesses(List<TelemetryData.ProcessData> processes) {
        if (processes == null) {
            return Collections.emptyList();
        }
        return processes.stream()
                .map(p -> Process.builder()
                        .pid(p.getPid())
                        .name(p.getName())
                        .cpuUsage(p.getCpu())
                        .username(p.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<NetworkConnection> mapConnections(List<TelemetryData.NetworkConnectionData> connections) {
        if (connections == null) {
            return Collections.emptyList();
        }
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

    private List<TelemetryResponse.ProcessResponse> mapProcessResponses(List<Process> processes) {
        if (processes == null) {
            return Collections.emptyList();
        }
        return processes.stream()
                .map(p -> new TelemetryResponse.ProcessResponse(p.getPid(), p.getName(), p.getCpuUsage(), p.getUsername()))
                .collect(Collectors.toList());
    }

    private List<TelemetryResponse.NetworkConnectionResponse> mapConnectionResponses(List<NetworkConnection> connections) {
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

    private String toTrend(double first, double last) {
        double delta = last - first;
        if (Math.abs(delta) < 2.0) {
            return "STABLE";
        }
        return delta > 0 ? "RISING" : "FALLING";
    }
}
